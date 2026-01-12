package com.miriki.ti99.imagetools.fs;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.miriki.ti99.imagetools.domain.*;
import com.miriki.ti99.imagetools.domain.io.FileFlagBits;
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
     */
    public static VolumeInformationBlock createFile(
            Ti99Image image,
            DiskFormat format,
            VolumeInformationBlock vib,
            String fileName,
            byte[] data) {

        // log.debug("createFile(image={}, format={}, vib={}, fileName={}, dataLen={})", image, format, vib, fileName, data.length);

        Objects.requireNonNull(image);
        Objects.requireNonNull(format);
        Objects.requireNonNull(vib);
        Objects.requireNonNull(fileName);
        Objects.requireNonNull(data);

        AllocationBitmap abm = vib.getAbm();

        // ------------------------------------------------------------
        // 1) Determine required sectors
        // ------------------------------------------------------------
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

        // ------------------------------------------------------------
        // 5) Build FDR
        // ------------------------------------------------------------
        FileDescriptorRecord fdr = buildFdr(fileName, allocatedSectors, data.length, format);

        // ------------------------------------------------------------
        // 6) Write FDR + update FDI
        // ------------------------------------------------------------
        FileDescriptorRecordIO.writeTo(image.getSector(fdrSector), fdr);
        addFdrToFdi(image, fileName, fdrSector);

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

        // log.debug("allocateSectors(abm={}, format={}, needed={})", abm, format, neededSectors);

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

        // log.debug("writeDataToSectors(image={}, sectors={}, dataLen={})", image, sectors, data.length);

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
        // log.debug("findFreeFdrSector(image={}, format={})", image, format);

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
            String fileName,
            List<Integer> allocatedSectors,
            int fileSize,
            DiskFormat format) {

        // log.debug("buildFdr(fileName={}, sectors={}, fileSize={}, format={})", fileName, allocatedSectors, fileSize, format);

        FileDescriptorRecord fdr = new FileDescriptorRecord();

        // Basic metadata
        fdr.setFileName(fileName);
        fdr.setFileType(FileFlagBits.TYPE_PROGRAM);
        fdr.setFlags(0);

        // HFDC fields
        fdr.setExtendedRecordLength(0);
        fdr.setRecordsPerSector(0);
        fdr.setLogicalRecordLength(0);
        fdr.setLevel3RecordsUsed(0);

        int timestamp = encodeFilDateTime(LocalDateTime.now());
        fdr.setCreationTimestamp(timestamp);
        fdr.setUpdateTimestamp(timestamp);

        // Sector info
        int sectors = allocatedSectors.size();
        fdr.setTotalSectorsAllocated(sectors);
        fdr.setUsedSectors(sectors);
        fdr.setUsedClusters(sectors);

        fdr.setFirstSector(allocatedSectors.get(0));
        fdr.setLastSector(allocatedSectors.get(sectors - 1));

        fdr.setEofOffset(fileSize % SECTOR_SIZE);
        fdr.setNextFdr(0);

        // Data chain (legacy)
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

        // log.debug("updateVibWithNewAbm(oldVib={}, abm={})", oldVib, abm);

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
