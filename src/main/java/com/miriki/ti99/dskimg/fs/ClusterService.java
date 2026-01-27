package com.miriki.ti99.dskimg.fs;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.miriki.ti99.dskimg.domain.*;
import com.miriki.ti99.dskimg.domain.constants.DiskLayoutConstants;
import com.miriki.ti99.dskimg.io.Cluster;
import com.miriki.ti99.dskimg.io.DataChainBlockIO;
import com.miriki.ti99.dskimg.io.Sector;

/**
 * High-level cluster management for TI-99 disk images.
 *
 * Responsibilities:
 *   - allocate clusters (contiguous groups of sectors)
 *   - write data into clusters
 *   - free clusters based on FDR data chains
 *
 * This version is fully FS-based and contains no directory logic.
 */
public final class ClusterService {

    private static final Logger log = LoggerFactory.getLogger(ClusterService.class);

    private ClusterService() {}

    // ============================================================
    //  PUBLIC API – ALLOCATE CLUSTERS
    // ============================================================

    public static List<Integer> allocateClusters(Ti99FileSystem fs, int neededClusters) {
        Objects.requireNonNull(fs, "filesystem must not be null");

        if (neededClusters <= 0) {
            throw new IllegalArgumentException("neededClusters must be > 0");
        }

        AllocationBitmap abm = requireAbm(fs);
        DiskFormat fmt = fs.getImage().getFormat();

        int totalClusters = fmt.getClusterCount();
        List<int[]> freeBlocks = new ArrayList<>();

        int start = -1;
        int length = 0;

        // 1) Scan free blocks
        for (int c = 0; c < totalClusters; c++) {
            if (isClusterFree(abm, fmt, c)) {
                if (start < 0) {
                    start = c;
                    length = 1;
                } else {
                    length++;
                }
            } else {
                if (start >= 0) {
                    freeBlocks.add(new int[]{start, length});
                    start = -1;
                }
            }
        }

        if (start >= 0) {
            freeBlocks.add(new int[]{start, length});
        }

        // 2) Best-Tight-Fit
        for (int[] block : freeBlocks) {
            if (block[1] == neededClusters) {
                return allocateBlock(abm, fmt, block[0], neededClusters);
            }
        }

        // 3) Best-Fit
        int bestStart = -1;
        int bestLen = Integer.MAX_VALUE;

        for (int[] block : freeBlocks) {
            if (block[1] > neededClusters && block[1] < bestLen) {
                bestStart = block[0];
                bestLen = block[1];
            }
        }

        if (bestStart >= 0) {
            return allocateBlock(abm, fmt, bestStart, neededClusters);
        }

        // 4) First-Fit (fragmenting)
        List<Integer> allocated = new ArrayList<>(neededClusters);

        for (int c = 0; c < totalClusters && allocated.size() < neededClusters; c++) {
            if (isClusterFree(abm, fmt, c)) {
                markClusterUsed(abm, fmt, c, true);
                allocated.add(c);
            }
        }

        if (allocated.size() != neededClusters) {
            throw new IllegalStateException("Not enough free clusters");
        }

        return allocated;
    }

    private static List<Integer> allocateBlock(AllocationBitmap abm, DiskFormat fmt,
                                               int start, int needed) {

        List<Integer> result = new ArrayList<>(needed);

        for (int c = start; c < start + needed; c++) {
            markClusterUsed(abm, fmt, c, true);
            result.add(c);
        }

        return result;
    }

    // ============================================================
    //  PUBLIC API – WRITE DATA INTO CLUSTERS
    // ============================================================

    public static void writeDataToClusters(Ti99FileSystem fs, List<Integer> clusters, byte[] data) {
        Objects.requireNonNull(fs, "filesystem must not be null");
        Objects.requireNonNull(clusters, "clusters must not be null");
        Objects.requireNonNull(data, "data must not be null");

        if (clusters.isEmpty()) {
            throw new IllegalArgumentException("clusters must not be empty");
        }

        Ti99Image img = fs.getImage();
        DiskFormat fmt = img.getFormat();

        int sectorsPerCluster = fmt.getSectorsPerCluster();
        int sectorSize = DiskLayoutConstants.SECTOR_SIZE;

        log.debug("Writing {} bytes into {} clusters", data.length, clusters.size());

        int pos = 0;

        for (int clusterIndex : clusters) {
            Cluster cluster = img.getCluster(clusterIndex);

            for (int s = 0; s < sectorsPerCluster; s++) {
                Sector sector = cluster.getSector(s);
                pos = writeSector(sector, data, pos, sectorSize);
            }
        }
    }

    // ============================================================
    //  PUBLIC API – FREE CLUSTERS
    // ============================================================

    public static void freeClusters(Ti99FileSystem fs, FileDescriptorRecord fdr) {
        Objects.requireNonNull(fs, "filesystem must not be null");
        Objects.requireNonNull(fdr, "fdr must not be null");

        AllocationBitmap abm = requireAbm(fs);
        Ti99Image image = fs.getImage();
        DiskFormat format = image.getFormat();

        // FDR sector via FS (not via IO)
        int fdrSector = fs.getSectorForFile(fdr.getFileName());

        if (fdrSector <= 0) {
            throw new IllegalStateException("FDI entry missing for: " + fdr.getFileName());
        }

        log.debug("Freeing clusters for '{}'", fdr.getFileName());

        List<Integer> oldClusters =
                DataChainBlockIO.readClusterChain(image, fdrSector);

        for (int cluster : oldClusters) {
            markClusterUsed(abm, format, cluster, false);
        }
    }

    // ============================================================
    //  INTERNAL HELPERS – ABM / FORMAT
    // ============================================================

    private static AllocationBitmap requireAbm(Ti99FileSystem fs) {
        AllocationBitmap abm = fs.getAbm();
        if (abm == null) {
            throw new IllegalStateException("AllocationBitmap (ABM) not set");
        }
        return abm;
    }

    private static boolean isClusterFree(AllocationBitmap abm, DiskFormat fmt, int cluster) {
        int firstSector = fmt.clusterToSector(cluster);
        int sectorsPerCluster = fmt.getSectorsPerCluster();

        for (int s = 0; s < sectorsPerCluster; s++) {
            if (abm.isUsed(firstSector + s)) {
                return false;
            }
        }
        return true;
    }

    private static void markClusterUsed(AllocationBitmap abm, DiskFormat fmt, int cluster, boolean used) {
        int firstSector = fmt.clusterToSector(cluster);
        int sectorsPerCluster = fmt.getSectorsPerCluster();

        for (int s = 0; s < sectorsPerCluster; s++) {
            abm.setUsed(firstSector + s, used);
        }
    }

    // ============================================================
    //  INTERNAL HELPERS – WRITE SECTOR
    // ============================================================

    private static int writeSector(Sector sector, byte[] data, int pos, int sectorSize) {
        for (int i = 0; i < sectorSize; i++) {
            byte value = (pos < data.length) ? data[pos++] : 0x00;
            sector.set(i, value);
        }
        return pos;
    }
}
