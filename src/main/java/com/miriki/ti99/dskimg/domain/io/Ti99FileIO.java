package com.miriki.ti99.dskimg.domain.io;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.miriki.ti99.dskimg.domain.DiskFormat;
import com.miriki.ti99.dskimg.domain.FileDescriptorIndex;
import com.miriki.ti99.dskimg.domain.FileDescriptorRecord;
import com.miriki.ti99.dskimg.domain.FileStatusBuilder;
import com.miriki.ti99.dskimg.domain.Ti99File;
import com.miriki.ti99.dskimg.domain.Ti99FileSystem;
import com.miriki.ti99.dskimg.domain.constants.DiskLayoutConstants;
import com.miriki.ti99.dskimg.domain.constants.FdiConstants;
import com.miriki.ti99.dskimg.fs.ClusterService;
import com.miriki.ti99.dskimg.io.AllocationBitmapIO;
import com.miriki.ti99.dskimg.io.DataChainBlockIO;
import com.miriki.ti99.dskimg.io.FileDescriptorIndexIO;
import com.miriki.ti99.dskimg.io.FileDescriptorRecordIO;
import com.miriki.ti99.dskimg.io.Sector;

public final class Ti99FileIO {

    private static final Logger log = LoggerFactory.getLogger(Ti99FileIO.class);

    private Ti99FileIO() {}

    // ============================================================
    //  PUBLIC API – READ FILE
    // ============================================================

    public static Ti99File readFile(Ti99FileSystem fs, FileDescriptorRecord fdr) {
        Objects.requireNonNull(fs);
        Objects.requireNonNull(fdr);

        DiskFormat format = fs.getImage().getFormat();
        int sectorSize = DiskLayoutConstants.SECTOR_SIZE;
        int sectorsPerCluster = format.getSectorsPerCluster();

        List<Integer> clusters = readClusterChain(fs, fdr);
        byte[] fullData = readContentFromClusters(fs, clusters, sectorsPerCluster, sectorSize);

        int totalSectors = clusters.size() * sectorsPerCluster;
        int fileLength = calculateFileLength(totalSectors, sectorSize, fdr.getEofOffset());

        byte[] content = Arrays.copyOf(fullData, fileLength);

        return buildTi99FileFromFdrAndContent(fdr, content);
    }

    // ============================================================
    //  PUBLIC API – WRITE FILE
    // ============================================================

    public static void writeFile(Ti99FileSystem fs, FileDescriptorRecord fdr, Ti99File file) {
        Objects.requireNonNull(fs);
        Objects.requireNonNull(fdr);
        Objects.requireNonNull(file);

        DiskFormat format = fs.getImage().getFormat();
        int sectorSize = DiskLayoutConstants.SECTOR_SIZE;
        int sectorsPerCluster = format.getSectorsPerCluster();

        byte[] data = file.getContent();

        freeExistingClustersIfAny(fs, fdr);

        int neededSectors = calculateNeededSectors(data.length, sectorSize);
        int neededClusters = calculateNeededClusters(neededSectors, sectorsPerCluster);

        List<Integer> newClusters = ClusterService.allocateClusters(fs, neededClusters);
        ClusterService.writeDataToClusters(fs, newClusters, data);

        int totalSectors = neededClusters * sectorsPerCluster;
        int eofOffset = data.length % sectorSize;
        int now = (int) Instant.now().getEpochSecond();

        FileDescriptorRecord updated = buildUpdatedFdr(fdr, file, newClusters, totalSectors, eofOffset, now);

        int fdrSector = fdrSectorIndex(fs, fdr);
        writeFdrToDisk(fs, updated, fdrSector);
        updateAllocationBitmap(fs);

        logUpdateInfo(updated);
    }

    // ============================================================
    //  PUBLIC API – DELETE FILE
    // ============================================================

    public static void deleteFile(Ti99FileSystem fs, FileDescriptorRecord fdr) {
        Objects.requireNonNull(fs);
        Objects.requireNonNull(fdr);

        ClusterService.freeClusters(fs, fdr);

        int fdrSector = fdrSectorIndex(fs, fdr);
        clearFdrSector(fs, fdrSector);
        clearFdiEntry(fs, fdrSector);
        writeFdi(fs);
        updateAllocationBitmap(fs);

        fs.getFiles().remove(fdr);
    }

    // ============================================================
    //  PUBLIC API – DIRECTORY READ
    // ============================================================

    public static List<Ti99File> readDirectory(Ti99FileSystem fs) {
        Objects.requireNonNull(fs);

        List<Ti99File> files = new ArrayList<>();
        FileDescriptorIndex fdi = fs.getFdi();
        int[] fdrSectors = fdi.getFdrSectors();

        for (int sector : fdrSectors) {
            if (sector == 0) continue;

            Sector s = fs.getImage().getSector(sector);
            FileDescriptorRecord fdr = FileDescriptorRecordIO.readFrom(s);
            if (fdr.isEmpty()) continue;

            Ti99File file = buildTi99FileFromFdrForDirectory(fdr);
            int dirSize = calculateFileLength(
                    fdr.getUsedSectors(),
                    DiskLayoutConstants.SECTOR_SIZE,
                    fdr.getEofOffset()
            );
            file.setDirSize(dirSize);

            files.add(file);
        }

        return files;
    }

    // ============================================================
    //  HELPER – CLUSTER / CONTENT
    // ============================================================

    private static List<Integer> readClusterChain(Ti99FileSystem fs, FileDescriptorRecord fdr) {
        int fdrSector = fdrSectorIndex(fs, fdr);
        return DataChainBlockIO.readClusterChain(fs.getImage(), fdrSector);
    }

    private static byte[] readContentFromClusters(Ti99FileSystem fs,
                                                  List<Integer> clusters,
                                                  int sectorsPerCluster,
                                                  int sectorSize) {

        byte[] fullData = new byte[clusters.size() * sectorsPerCluster * sectorSize];
        int pos = 0;

        for (int clusterIndex : clusters) {
            int firstSector = fs.getImage().getFormat().clusterToSector(clusterIndex);

            for (int s = 0; s < sectorsPerCluster; s++) {
                int sectorNumber = firstSector + s;
                Sector sec = fs.getImage().getSector(sectorNumber);

                byte[] sectorData = sec.getBytes();
                int len = Math.min(sectorSize, sectorData.length);

                System.arraycopy(sectorData, 0, fullData, pos, len);
                pos += len;
            }
        }

        return fullData;
    }

    // ============================================================
    //  HELPER – FILE LENGTH / CLUSTER CALCULATION
    // ============================================================

    private static int calculateFileLength(int totalSectors, int sectorSize, int eofOffset) {
        if (totalSectors <= 0) return 0;
        if (eofOffset == 0) return totalSectors * sectorSize;
        return (totalSectors - 1) * sectorSize + eofOffset;
    }

    private static int calculateNeededSectors(int byteLength, int sectorSize) {
        if (byteLength <= 0) return 0;
        return (byteLength + sectorSize - 1) / sectorSize;
    }

    private static int calculateNeededClusters(int neededSectors, int sectorsPerCluster) {
        if (neededSectors <= 0) return 0;
        return (neededSectors + sectorsPerCluster - 1) / sectorsPerCluster;
    }

    // ============================================================
    //  HELPER – FDR UPDATE / BUILD
    // ============================================================

    private static FileDescriptorRecord buildUpdatedFdr(
            FileDescriptorRecord original,
            Ti99File file,
            List<Integer> newClusters,
            int totalSectors,
            int eofOffset,
            int timestamp) {

        int newStatus = FileStatusBuilder.buildStatusByte(file);

        return original
                .withFileStatus(newStatus)
                .withUsedSectors(totalSectors)
                .withEofOffset(eofOffset)
                .withUpdateDate(timestamp)
                .withDataChain(newClusters);
    }

    /*
    public static FileType parseFileType(String s) {
        if (s == null) return null;

        s = s.trim().toUpperCase();

        return switch (s) {
            case "PROGRAM", "PGM" -> FileType.PGM;
            case "DIS", "DISPLAY" -> FileType.DIS;
            case "INT", "INTERNAL" -> FileType.INT;
            default -> throw new IllegalArgumentException("Unknown file type: " + s);
        };
    }
    */
    
    private static Ti99File buildTi99FileFromFdrAndContent(FileDescriptorRecord fdr, byte[] content) {
        Ti99File file = new Ti99File();
        file.setFileName(fdr.getFileName());
        file.setType(FileTypeIO.decode(fdr.getFileStatus()));
        file.setContent(content);
        file.setFlags(fdr.getFileStatus());
        file.setRecordLength(fdr.getLogicalRecordLength());
        file.setRecordsPerSector(fdr.getRecordsPerSector());
        file.setTimestamp(fdr.getUpdateTimestamp());
        return file;
    }

    private static Ti99File buildTi99FileFromFdrForDirectory(FileDescriptorRecord fdr) {
        Ti99File file = new Ti99File();
        file.setFileName(fdr.getFileName());
        file.setType(FileTypeIO.decode(fdr.getFileStatus()));
        file.setFlags(fdr.getFileStatus());
        file.setRecordLength(fdr.getLogicalRecordLength());
        file.setRecordsPerSector(fdr.getRecordsPerSector());
        file.setTimestamp(fdr.getUpdateTimestamp());
        return file;
    }

    // ============================================================
    //  HELPER – CLUSTER MANAGEMENT / FDR WRITE
    // ============================================================

    private static void freeExistingClustersIfAny(Ti99FileSystem fs, FileDescriptorRecord fdr) {
        if (!fdr.getDataChain().isEmpty()) {
            ClusterService.freeClusters(fs, fdr);
        }
    }

    private static void writeFdrToDisk(Ti99FileSystem fs, FileDescriptorRecord updated, int fdrSector) {
        FileDescriptorRecordIO.writeTo(fs.getImage().getSector(fdrSector), updated);
        fs.getAbm().setUsed(fdrSector, true);
    }

    private static void updateAllocationBitmap(Ti99FileSystem fs) {
        int vibSector = fs.getImage().getFormat().getVibSector();
        AllocationBitmapIO.write(fs.getImage().getSector(vibSector), fs.getAbm());
    }

    private static void logUpdateInfo(FileDescriptorRecord updated) {
        log.info("Allocated sectors = {}", updated.getUsedSectors());
        log.info("EOF offset = {}", updated.getEofOffset());
        log.info("Updated timestamp = {}", updated.getUpdateTimestamp());
    }

    // ============================================================
    //  HELPER – DELETE SUPPORT
    // ============================================================

    private static void clearFdrSector(Ti99FileSystem fs, int fdrSector) {
        Sector s = fs.getImage().getSector(fdrSector);
        byte[] empty = new byte[s.getSize()];
        Arrays.fill(empty, (byte) DiskLayoutConstants.ZERO_BYTE);
        s.setData(empty);
    }

    private static void clearFdiEntry(Ti99FileSystem fs, int fdrSector) {
        FileDescriptorIndex fdi = fs.getFdi();
        int[] fdrSectors = fdi.getFdrSectors();

        for (int i = 0; i < fdrSectors.length; i++) {
            if (fdrSectors[i] == fdrSector) {
                fdi.setFdrSectorForFile(i, 0);
                break;
            }
        }
    }

    private static void writeFdi(Ti99FileSystem fs) {
        int fdiSector = fs.getImage().getFormat().getFdiSector();
        FileDescriptorIndexIO.writeTo(fs.getImage().getSector(fdiSector), fs.getFdi());
    }

    // ============================================================
    //  HELPER – FDR SECTOR INDEX
    // ============================================================

    private static int fdrSectorIndex(Ti99FileSystem fs, FileDescriptorRecord fdr) {
        int index = fs.getFiles().indexOf(fdr);
        if (index < 0) throw new IllegalArgumentException("FDR not found in filesystem");

        FileDescriptorIndex fdi = fs.getFdi();
        byte[] entries = fdi.getEntries();
        int offset = index * FdiConstants.FDI_ENTRY_SIZE;

        int hi = entries[offset] & 0xFF;
        int lo = entries[offset + 1] & 0xFF;

        return (hi << 8) | lo;
    }
}
