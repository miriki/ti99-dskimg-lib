package com.miriki.ti99.imagetools.fs;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.miriki.ti99.imagetools.domain.*;
import com.miriki.ti99.imagetools.io.FileDescriptorRecordIO;
import com.miriki.ti99.imagetools.io.Sector;
import com.miriki.ti99.imagetools.io.VolumeInformationBlockIO;

/**
 * High-level file writer for TI-99 disk images.
 *
 * Responsibilities:
 *   - allocate sectors for file data
 *   - write data into sectors
 *   - create and write File Descriptor Records (FDR)
 *   - update Allocation Bitmap (ABM)
 *   - update File Directory Information (FDI)
 *   - update Volume Information Block (VIB)
 *
 * Simplifications:
 *   - one FDR per file (no FDR chains)
 *   - sequential sector allocation
 *   - no hole reuse (but extendable)
 */
public final class FileWriter {

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(FileWriter.class);

    private static final int SECTOR_SIZE = 256;

    private FileWriter() {
        // Utility class – no instances
    }

    // ============================================================
    //  PUBLIC API – CREATE FILE
    // ============================================================

    /**
     * Creates a new file on the disk image.
     *
     * Uses Ti99File as high-level DTO:
     *   - fileName, recordLength, flags, content/packedContent
     *   - FIAD flags are taken as-is
     *   - FDR type is derived here from semantics (PROGRAM/DIS/INT, FIX/VAR)
     */
    public static VolumeInformationBlock createFile(
            Ti99Image image,
            DiskFormat format,
            VolumeInformationBlock vib,
            Ti99File file) {

        Objects.requireNonNull(image);
        Objects.requireNonNull(format);
        Objects.requireNonNull(vib);
        Objects.requireNonNull(file);

        AllocationBitmap abm = vib.getAbm();

        // ------------------------------------------------------------
        // 1) Determine required sectors (based on packed content)
        // ------------------------------------------------------------
        byte[] data = file.getPackedContent();
        int neededSectors = Math.max(1, (data.length + SECTOR_SIZE - 1) / SECTOR_SIZE);

        // ------------------------------------------------------------
        // 2) Allocate sectors
        // ------------------------------------------------------------
        List<Integer> allocatedSectors = allocateSectors(abm, format, neededSectors);

        if (allocatedSectors.size() < neededSectors) {
            throw new IllegalStateException("Not enough free sectors to allocate file");
        }

        // ------------------------------------------------------------
        // 3) Write file data into allocated sectors
        // ------------------------------------------------------------
        writeDataToSectors(image, allocatedSectors, data);

        // ------------------------------------------------------------
        // 4) Find free FDR sector
        // ------------------------------------------------------------
        int fdrSector = findFreeFdrSector(image, format);
        if (fdrSector < 0) {
            throw new IllegalStateException("No free FDR sector available");
        }

        abm.allocate(fdrSector);

        // DEBUG: Dump BEFORE writing FDR
        {
            Sector s = image.getSector(fdrSector);
            byte[] raw = s.getBytes();
            System.out.println("FDR HEX DUMP (before write), sector " + fdrSector + ":");
            for (int i = 0; i < 32; i++) {
                System.out.printf("%02X ", raw[i]);
            }
            System.out.println("\n");
        }

        // ------------------------------------------------------------
        // 5) Build FDR (derive FDR type here)
        // ------------------------------------------------------------
        FileDescriptorRecord fdr = buildFdr(file, allocatedSectors, format);

        // ------------------------------------------------------------
        // 6) Write FDR + update FDI
        // ------------------------------------------------------------
        Sector fdrSecObj = image.getSector(fdrSector);
        FileDescriptorRecordIO.writeTo(fdrSecObj, fdr);

        // DEBUG: Dump AFTER writing FDR
        {
        	byte actualStatus = image.getSector(fdrSector).getBytes()[0x0C];
        	System.out.printf("FDR fileStatus written: 0x%02X\n", actualStatus);
            byte[] raw = fdrSecObj.getBytes();
            System.out.println("FDR HEX DUMP (after write), sector " + fdrSector + ":");
            for (int i = 0; i < 32; i++) {
                System.out.printf("%02X ", raw[i]);
            }
            System.out.println("\n");
        }

        addFdrToFdi(image, file.getFileName(), fdrSector);

        // ------------------------------------------------------------
        // 7) Update VIB (ABM changed)
        // ------------------------------------------------------------
        VolumeInformationBlock updatedVib = updateVibWithNewAbm(vib, abm);
        VolumeInformationBlockIO.writeTo(image.getSector(format.getVibSector()), updatedVib);

        return updatedVib;
    }

    // ============================================================
    //  PUBLIC API – DELETE FILE
    // ============================================================

    public static VolumeInformationBlock deleteFile(
            Ti99Image image,
            DiskFormat format,
            VolumeInformationBlock vib,
            String fileName) {

        Objects.requireNonNull(image);
        Objects.requireNonNull(format);
        Objects.requireNonNull(vib);
        Objects.requireNonNull(fileName);

        AllocationBitmap abm = vib.getAbm();

        // 1) Find FDR
        int fdrSector = findFdrByName(image, format, fileName);
        if (fdrSector < 0) {
            throw new IllegalStateException("File not found: " + fileName);
        }

        // 2) Load FDR
        FileDescriptorRecord fdr = FileDescriptorRecordIO.readFrom(image.getSector(fdrSector));

        // 3) Free data sectors
        freeSectorsFromFdr(abm, fdr);

        // 4) Free FDR sector
        abm.free(fdrSector);

        // 5) Clear FDR sector
        clearSector(image.getSector(fdrSector));

        // 6) Update FDI
        removeFdrFromFdi(image, fileName);

        // 7) Update VIB
        VolumeInformationBlock updatedVib = updateVibWithNewAbm(vib, abm);
        VolumeInformationBlockIO.writeTo(image.getSector(format.getVibSector()), updatedVib);

        return updatedVib;
    }

    // ============================================================
    //  PUBLIC API – RENAME FILE
    // ============================================================

    public static void renameFile(
            Ti99Image image,
            DiskFormat format,
            String oldName,
            String newName) {

        Objects.requireNonNull(image);
        Objects.requireNonNull(format);
        Objects.requireNonNull(oldName);
        Objects.requireNonNull(newName);

        // 1) Find FDR
        int fdrSector = findFdrByName(image, format, oldName);
        if (fdrSector < 0) {
            throw new IllegalStateException("File not found: " + oldName);
        }

        // 2) Load FDR
        Sector fdrSec = image.getSector(fdrSector);
        FileDescriptorRecord fdr = FileDescriptorRecordIO.readFrom(fdrSec);

        // 3) Update name
        fdr.setFileName(newName);

        // 4) Write back
        FileDescriptorRecordIO.writeTo(fdrSec, fdr);

        // 5) Update FDI
        renameFdrInFdi(image, oldName, newName);
    }

    // ============================================================
    //  INTERNAL – SECTOR ALLOCATION
    // ============================================================

    private static List<Integer> allocateSectors(
            AllocationBitmap abm,
            DiskFormat format,
            int neededSectors) {

        List<Integer> sectors = new ArrayList<>(neededSectors);

        int firstDataSector = format.getFirstDataSector();
        int totalSectors = format.getTotalSectors();

        for (int sector = firstDataSector;
             sector < totalSectors && sectors.size() < neededSectors;
             sector++) {

            if (!abm.isUsed(sector)) {
                abm.allocate(sector);
                sectors.add(sector);
            }
        }

        return sectors;
    }

    // ============================================================
    //  INTERNAL – WRITE DATA TO SECTORS
    // ============================================================

    private static void writeDataToSectors(
            Ti99Image image,
            List<Integer> sectors,
            byte[] data) {

        int offset = 0;

        for (int sectorNumber : sectors) {
            Sector sector = image.getSector(sectorNumber);
            byte[] raw = sector.getBytes();

            int remaining = data.length - offset;
            int toCopy = Math.min(remaining, SECTOR_SIZE);

            if (toCopy > 0) {
                System.arraycopy(data, offset, raw, 0, toCopy);
                offset += toCopy;
            }

            // Fill remainder with zeros
            for (int i = toCopy; i < SECTOR_SIZE; i++) {
                raw[i] = 0x00;
            }

            // Write back
            for (int i = 0; i < SECTOR_SIZE; i++) {
                sector.set(i, raw[i]);
            }
        }
    }

    // ============================================================
    //  INTERNAL – FIND FDR SECTORS
    // ============================================================

    private static int findFreeFdrSector(Ti99Image image, DiskFormat format) {
        int firstFdr = format.getFirstFdrSector();
        int count = format.getFdrSectorCount();

        for (int i = 0; i < count; i++) {
            int sectorNumber = firstFdr + i;
            FileDescriptorRecord fdr = FileDescriptorRecordIO.readFrom(image.getSector(sectorNumber));
            if (fdr.getFileName().trim().isEmpty()) {
                return sectorNumber;
            }
        }

        return -1;
    }

    private static int findFdrByName(Ti99Image image, DiskFormat format, String fileName) {
        int firstFdr = format.getFirstFdrSector();
        int count = format.getFdrSectorCount();

        for (int i = 0; i < count; i++) {
            int sectorNumber = firstFdr + i;
            FileDescriptorRecord fdr = FileDescriptorRecordIO.readFrom(image.getSector(sectorNumber));
            if (fdr.getFileName().trim().equalsIgnoreCase(fileName)) {
                return sectorNumber;
            }
        }

        return -1;
    }

    // ============================================================
    //  INTERNAL – BUILD FDR
    // ============================================================

    public static FileDescriptorRecord buildFdr(
            Ti99File file,
            List<Integer> allocatedSectors,
            DiskFormat format) {

        FileDescriptorRecord fdr = new FileDescriptorRecord();
        byte[] data = file.getPackedContent();
        int packedSize = data.length;

        // --------------------------------------------------------
        // Basic metadata
        // --------------------------------------------------------
        fdr.setFileName(file.getFileName());

        // FIAD flags (TIFILES header) are taken as-is
        // fdr.setFlags(file.getFlags());

        // --------------------------------------------------------
        // FDR type (on-disk TI-DOS type code)
        // Derived from semantics: PROGRAM vs INTERNAL vs DISPLAY, FIX vs VAR
        // --------------------------------------------------------
        
        int status = 0;

	    // Bit 0: Program/Data (0 = Program, 1 = Data)
        if (file.isProgram()) status |= 0x01;
	
	    // Bit 1: ASCII/Binary (0 = DISPLAY, 1 = INTERNAL/PROGRAM)
        if (file.isInternal()) status |= 0x02;
	
	    // Bit 3: PROTECT (optional, wenn du sowas modellierst)
	    if (file.tfiIsProtected()) status |= 0x08;
	
	    // Bit 4: BACKUP (aus FIAD-Flag)
	    if (file.tfiIsBackup()) status |= 0x10;
	
	    // Bit 5: EMULATE (aus FIAD-Flag)
	    if (file.tfiIsEmulated()) status |= 0x20;
	
	    // Bit 7: FIXED/VARIABLE (0 = FIXED, 1 = VARIABLE)
	    if (file.isVariable()) status |= 0x80;
	
	    fdr.setFileStatus(status);
        
        // --------------------------------------------------------
        // HFDC / record-related fields
        // --------------------------------------------------------
        int recordLength = file.getRecordLength();

        // Extended record length (HFDC) – not used here
        fdr.setExtendedRecordLength(0);

        // Records per sector & logical record length:
        // For FIXED files: recordsPerSector = SECTOR_SIZE / recordLength
        // For VARIABLE or PROGRAM: 0
        int rps = 0;
        int lrl = 0;
        if (file.isProgram()) {
            rps = 0;
            lrl = 0;
        } else if (file.isFixed() && recordLength > 0) {
            rps = SECTOR_SIZE / recordLength; // z.B. 3 bei 80 Byte
            lrl = recordLength;
        } else if (file.isVariable() && recordLength > 0) {
            rps = 1;
            lrl = recordLength;
        } else {
            // optional: Fehlerfall
        }
        fdr.setRecordsPerSector(rps);
        fdr.setLogicalRecordLength(lrl);
        fdr.setLevel3RecordsUsed(0);

        // --------------------------------------------------------
        // Timestamps
        // --------------------------------------------------------
        int timestamp = encodeFilDateTime(LocalDateTime.now());
        fdr.setCreationTimestamp(timestamp);
        fdr.setUpdateTimestamp(timestamp);

        // --------------------------------------------------------
        // Sector info
        // --------------------------------------------------------
        int sectors = allocatedSectors.size();
        fdr.setTotalSectorsAllocated(sectors);
        fdr.setUsedSectors(sectors);
        fdr.setUsedClusters(sectors);

        fdr.setFirstSector(allocatedSectors.get(0));
        fdr.setLastSector(allocatedSectors.get(sectors - 1));

        // EOF offset: offset of last valid byte in last sector
        fdr.setEofOffset(packedSize % SECTOR_SIZE);

        // No FDR chaining for now
        fdr.setNextFdr(0);

        // Data chain (legacy / convenience)
        fdr.setDataChain(allocatedSectors);

        return fdr;
    }

    // ============================================================
    //  INTERNAL – FREE SECTORS
    // ============================================================

    private static void freeSectorsFromFdr(AllocationBitmap abm, FileDescriptorRecord fdr) {
        for (int sec : fdr.getDataChain()) {
            abm.free(sec);
        }
    }

    private static void clearSector(Sector sector) {
        for (int i = 0; i < SECTOR_SIZE; i++) {
            sector.set(i, (byte) 0x00);
        }
    }

    // ============================================================
    //  INTERNAL – TIMESTAMP ENCODING
    // ============================================================

    private static int encodeFilDateTime(LocalDateTime dt) {
        int year = dt.getYear();
        int month = dt.getMonthValue();
        int day = dt.getDayOfMonth();
        int hour = dt.getHour();
        int minute = dt.getMinute();
        int second = dt.getSecond() / 2; // 2-second resolution

        int nSecond = ((year - 1900) << 9)
                | (month << 5)
                | day;

        int nFirst = (hour << 11)
                | (minute << 5)
                | second;

        return (nFirst << 16) | (nSecond & 0xFFFF);
    }

    // ============================================================
    //  INTERNAL – FDI UPDATE
    // ============================================================

    private static void addFdrToFdi(Ti99Image image, String fileName, int fdrSector) {
        DiskFormat format = image.getFormat();
        Sector fdiSector = image.getSector(format.getFdiSector());

        FdiDirectory dir = FdiDirectory.readFrom(fdiSector);

        // Ensure names are populated
        for (FdiDirectory.DirectoryEntry e : dir.getEntries()) {
            if (e.name() == null) {
                FileDescriptorRecord fdr = FileDescriptorRecordIO.readFrom(image.getSector(e.fdrSector()));
                dir.setNameForSector(e.fdrSector(), fdr.getFileName());
            }
        }

        dir.add(fileName, fdrSector);
        dir.writeTo(fdiSector);
    }

    private static void removeFdrFromFdi(Ti99Image image, String fileName) {
        DiskFormat format = image.getFormat();
        Sector fdiSector = image.getSector(format.getFdiSector());

        FdiDirectory dir = FdiDirectory.readFrom(fdiSector);
        dir.remove(fileName);
        dir.writeTo(fdiSector);
    }

    private static void renameFdrInFdi(Ti99Image image, String oldName, String newName) {
        DiskFormat format = image.getFormat();
        Sector fdiSector = image.getSector(format.getFdiSector());

        FdiDirectory dir = FdiDirectory.readFrom(fdiSector);
        dir.rename(oldName, newName);
        dir.writeTo(fdiSector);
    }

    // ============================================================
    //  INTERNAL – UPDATE VIB
    // ============================================================

    private static VolumeInformationBlock updateVibWithNewAbm(
            VolumeInformationBlock oldVib,
            AllocationBitmap abm) {

        return new VolumeInformationBlock(
                oldVib.getVolumeName(),
                oldVib.getTotalSectors(),
                oldVib.getSectorsPerTrack(),
                oldVib.getTracksPerSide(),
                oldVib.getSides(),
                oldVib.getDensity(),
                oldVib.getSignature(),
                oldVib.getDir1Name(),
                oldVib.getDir1Pointer(),
                oldVib.getDir2Name(),
                oldVib.getDir2Pointer(),
                oldVib.getDir3Name(),
                oldVib.getDir3Pointer(),
                abm
        );
    }
}
