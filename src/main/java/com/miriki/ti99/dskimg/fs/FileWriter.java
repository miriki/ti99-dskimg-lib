package com.miriki.ti99.dskimg.fs;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.miriki.ti99.dskimg.domain.*;
import com.miriki.ti99.dskimg.domain.constants.DiskLayoutConstants;
import com.miriki.ti99.dskimg.io.*;

/**
 * Modernized high-level file writer for TI-99 disk images.
 *
 * Responsibilities:
 *   - allocate clusters (via ClusterService)
 *   - write file data into clusters
 *   - create and write File Descriptor Records (FDR)
 *   - write HFDC Data Chain Pointer (DCP) block
 *   - update FDI (File Directory Information)
 *   - update VIB (Volume Information Block)
 *
 * This version is fully FS-based and cluster-based.
 */
public final class FileWriter {

    private static final Logger log = LoggerFactory.getLogger(FileWriter.class);
    private static final int SECTOR_SIZE = DiskLayoutConstants.SECTOR_SIZE;

    private FileWriter() {}

    // ============================================================
    //  PUBLIC API – CREATE FILE
    // ============================================================

    public static VolumeInformationBlock createFile(
            Ti99FileSystem fs,
            Ti99File file) {

        Objects.requireNonNull(fs,   "filesystem must not be null");
        Objects.requireNonNull(file, "file must not be null");

        fs.validate();

        Ti99Image image = fs.getImage();
        DiskFormat format = image.getFormat();
        VolumeInformationBlock vib = fs.getVib();
        AllocationBitmap abm = fs.getAbm();

        byte[] data = file.getPackedContent();

        log.debug("Creating file '{}' ({} bytes)", file.getFileName(), data.length);

        // ------------------------------------------------------------
        // 1) Determine required clusters
        // ------------------------------------------------------------
        int clusterSize = format.getSectorsPerCluster() * SECTOR_SIZE;
        int neededClusters = Math.max(1, (data.length + clusterSize - 1) / clusterSize);

        // ------------------------------------------------------------
        // 2) Allocate clusters
        // ------------------------------------------------------------
        List<Integer> clusters = ClusterService.allocateClusters(fs, neededClusters);

        // ------------------------------------------------------------
        // 3) Write file data into clusters
        // ------------------------------------------------------------
        ClusterService.writeDataToClusters(fs, clusters, data);

        // ------------------------------------------------------------
        // 4) Allocate FDR sector via FDI
        // ------------------------------------------------------------
        int fdrSector = allocateFdrSector(image, format);

        // ------------------------------------------------------------
        // 5) Build FDR
        // ------------------------------------------------------------
        FileDescriptorRecord fdr = buildFdr(file, clusters, format);
        log.debug("Allocated FDR sector: {}", fdrSector);

        // ------------------------------------------------------------
        // 6) Write FDR
        // ------------------------------------------------------------
        FileDescriptorRecordIO.writeTo(image.getSector(fdrSector), fdr);

        // ------------------------------------------------------------
        // 7) Write HFDC Data Chain Pointer block
        // ------------------------------------------------------------
        DataChainBlockIO.writeClusterChain(image, fdrSector, clusters);

        // ------------------------------------------------------------
        // 8) Update FDI
        // ------------------------------------------------------------
        updateFdi(image, format, file.getFileName(), fdrSector);

        // Reload updated FDI into FS
        FileDescriptorIndex newFdi = FileDescriptorIndexIO.readFrom(image.getSector(format.getFdiSector()));
        fs.setFdi(newFdi);

        // Add FDR to filesystem model
        fs.addFile(fdr);

        // ------------------------------------------------------------
        // 9) Update VIB (ABM changed)
        // ------------------------------------------------------------
        VolumeInformationBlock updatedVib = updateVibWithNewAbm(vib, abm);
        VolumeInformationBlockIO.writeTo(image.getSector(format.getVibSector()), updatedVib);
        fs.setVib(updatedVib);

        log.info("File created: {}", file.getFileName());

        return updatedVib;
    }

    // ============================================================
    //  PUBLIC API – DELETE FILE (FS-based)
    // ============================================================

    public static VolumeInformationBlock deleteFile(
            Ti99FileSystem fs,
            String fileName) {

        Objects.requireNonNull(fs, "filesystem must not be null");
        Objects.requireNonNull(fileName, "fileName must not be null");

        fs.validate();

        Ti99Image image = fs.getImage();
        DiskFormat format = image.getFormat();
        AllocationBitmap abm = fs.getAbm();
        VolumeInformationBlock vib = fs.getVib();

        // 1) Find FDR via FS model
        FileDescriptorRecord fdr = fs.getFileByName(fileName);
        if (fdr == null) {
            throw new IllegalStateException("File not found: " + fileName);
        }

        // 2) Determine FDR sector via FDI
        int fdrSector = fs.getSectorForFile(fileName);
        if (fdrSector <= 0) {
            throw new IllegalStateException("FDI entry missing for: " + fileName);
        }

        // 3) Free clusters
        List<Integer> clusters = DataChainBlockIO.readClusterChain(image, fdrSector);
        for (int c : clusters) {
            abm.freeCluster(format, c);
        }

        // 4) Free FDR sector
        abm.free(fdrSector);

        // 5) Clear FDR sector
        clearSector(image.getSector(fdrSector));

        // 6) Update FDI
        removeFromFdi(image, format, fileName);

        // Reload updated FDI into FS
        FileDescriptorIndex newFdi =
                FileDescriptorIndexIO.readFrom(image.getSector(format.getFdiSector()));
        fs.setFdi(newFdi);

        // 7) Remove file from FS model
        fs.removeFileByName(fileName);

        // 8) Update VIB
        VolumeInformationBlock updatedVib = updateVibWithNewAbm(vib, abm);
        VolumeInformationBlockIO.writeTo(image.getSector(format.getVibSector()), updatedVib);
        fs.setVib(updatedVib);

        log.info("File deleted: {}", fileName);

        return updatedVib;
    }

    // ============================================================
    //  PUBLIC API – RENAME FILE (FS-based)
    // ============================================================

    public static void renameFile(
            Ti99FileSystem fs,
            String oldName,
            String newName) {

        Objects.requireNonNull(fs, "filesystem must not be null");
        Objects.requireNonNull(oldName, "oldName must not be null");
        Objects.requireNonNull(newName, "newName must not be null");

        fs.validate();

        Ti99Image image = fs.getImage();
        DiskFormat format = image.getFormat();

        // 1) Find FDR via FS
        FileDescriptorRecord fdr = fs.getFileByName(oldName);
        if (fdr == null) {
            throw new IllegalStateException("File not found: " + oldName);
        }

        // 2) Determine FDR sector
        int fdrSector = fs.getSectorForFile(oldName);
        if (fdrSector <= 0) {
            throw new IllegalStateException("FDI entry missing for: " + oldName);
        }

        // 3) Update FDR on disk
        Sector fdrSec = image.getSector(fdrSector);
        fdr.setFileName(newName);
        FileDescriptorRecordIO.writeTo(fdrSec, fdr);

        // 4) Update FDI
        renameInFdi(image, format, oldName, newName);

        // Reload updated FDI into FS
        FileDescriptorIndex newFdi =
                FileDescriptorIndexIO.readFrom(image.getSector(format.getFdiSector()));
        fs.setFdi(newFdi);

        // 5) Update FS model
        fs.renameFile(oldName, newName);

        log.info("File renamed: {} → {}", oldName, newName);
    }

    // ============================================================
    //  FIXED RECORD PACKING
    // ============================================================

    public static byte[] buildFixStream(byte[] content, int recLen) {

        int recordCount = content.length / recLen;
        int recordsPerSector = SECTOR_SIZE / recLen;
        int totalSectors = (recordCount + recordsPerSector - 1) / recordsPerSector;

        ByteArrayOutputStream out = new ByteArrayOutputStream(totalSectors * SECTOR_SIZE);

        int offset = 0;

        for (int s = 0; s < totalSectors; s++) {
            int bytesWritten = 0;

            for (int r = 0; r < recordsPerSector; r++) {
                if (offset + recLen > content.length) break;

                out.write(content, offset, recLen);
                offset += recLen;
                bytesWritten += recLen;
            }

            while (bytesWritten < SECTOR_SIZE) {
                out.write(0x00);
                bytesWritten++;
            }
        }

        return out.toByteArray();
    }

    // ============================================================
    //  INTERNAL – FDR SECTOR ALLOCATION
    // ============================================================

    private static int allocateFdrSector(Ti99Image image, DiskFormat format) {
        Sector fdiSector = image.getSector(format.getFdiSector());
        FdiDirectoryIO dir = FdiDirectoryIO.readFrom(fdiSector, image);

        List<FdiDirectoryIO.DirectoryEntry> entries = dir.getEntries();

        int first = format.getFirstFdrSector();
        int last  = format.getLastFdrSector();

        for (int s = first; s <= last; s++) {
            int sector = s;
            boolean used = entries.stream().anyMatch(e -> e.fdrSector() == sector);
            if (!used) {
                return sector;
            }
        }

        throw new IllegalStateException("No free FDR sector available");
    }

    // ============================================================
    //  INTERNAL – FDR BUILDING
    // ============================================================

    private static FileDescriptorRecord buildFdr(
            Ti99File file,
            List<Integer> clusters,
            DiskFormat format) {

        FileDescriptorRecord fdr = new FileDescriptorRecord();
        byte[] data = file.getPackedContent();

        fdr.setFileName(file.getFileName());
        fdr.setFileStatus(file.toFdrStatusByte());

        int recordLength = file.getRecordLength();
        // int clusterSize = format.getSectorsPerCluster() * SECTOR_SIZE;
        int clusterSize = (format != null)
                ? format.getSectorsPerCluster() * SECTOR_SIZE
                : SECTOR_SIZE; // default für Tests

        if (file.isFixed()) {
            fdr.setRecordsPerSector(SECTOR_SIZE / recordLength);
            fdr.setLogicalRecordLength(recordLength);
            fdr.setEofOffset(0);
        }
        else if (file.isVariable()) {
            fdr.setRecordsPerSector(1);
            fdr.setLogicalRecordLength(recordLength);
            fdr.setEofOffset(data.length % clusterSize);
        }
        else {
            fdr.setRecordsPerSector(0);
            fdr.setLogicalRecordLength(0);
            fdr.setEofOffset(data.length % clusterSize);
        }

        int timestamp = encodeFilDateTime(LocalDateTime.now());
        fdr.setCreationTimestamp(timestamp);
        fdr.setUpdateTimestamp(timestamp);

        fdr.setTotalSectorsAllocated(clusters.size() * format.getSectorsPerCluster());
        fdr.setUsedSectors(fdr.getTotalSectorsAllocated());
        fdr.setUsedClusters(clusters.size());
        fdr.setNextFdr(0);

        return fdr;
    }

    public static FileDescriptorRecord buildFdrForTest(
            Ti99File file,
            List<Integer> clusters
    ) {
        return buildFdr(file, clusters, null);
    }

    // ============================================================
    //  INTERNAL – FDI UPDATE
    // ============================================================

    private static void updateFdi(Ti99Image image, DiskFormat format, String name, int fdrSector) {
        Sector fdiSector = image.getSector(format.getFdiSector());
        FdiDirectoryIO dir = FdiDirectoryIO.readFrom(fdiSector, image);
        dir.add(name.toUpperCase(), fdrSector);
        dir.writeTo(fdiSector);
    }

    private static void removeFromFdi(Ti99Image image, DiskFormat format, String name) {
        Sector fdiSector = image.getSector(format.getFdiSector());
        FdiDirectoryIO dir = FdiDirectoryIO.readFrom(fdiSector, image);
        dir.remove(name.toUpperCase());
        dir.writeTo(fdiSector);
    }

    private static void renameInFdi(Ti99Image image, DiskFormat format, String oldName, String newName) {
        Sector fdiSector = image.getSector(format.getFdiSector());
        FdiDirectoryIO dir = FdiDirectoryIO.readFrom(fdiSector, image);
        dir.rename(oldName.toUpperCase(), newName.toUpperCase());
        dir.writeTo(fdiSector);
    }

    // ============================================================
    //  INTERNAL – UTILITY
    // ============================================================

    private static void clearSector(Sector sector) {
        for (int i = 0; i < SECTOR_SIZE; i++) {
            sector.set(i, (byte) 0x00);
        }
    }

    private static int encodeFilDateTime(LocalDateTime dt) {
        int year = dt.getYear();
        int month = dt.getMonthValue();
        int day = dt.getDayOfMonth();
        int hour = dt.getHour();
        int minute = dt.getMinute();
        int second = dt.getSecond() / 2;

        int nSecond = ((year - 1900) << 9) | (month << 5) | day;
        int nFirst = (hour << 11) | (minute << 5) | second;

        return (nFirst << 16) | (nSecond & 0xFFFF);
    }

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
