package com.miriki.ti99.imagetools.fs;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.miriki.ti99.imagetools.domain.AllocationBitmap;
import com.miriki.ti99.imagetools.domain.DiskFormat;
import com.miriki.ti99.imagetools.domain.FileDescriptorRecord;
import com.miriki.ti99.imagetools.domain.Ti99FileSystem;
import com.miriki.ti99.imagetools.domain.Ti99Image;
import com.miriki.ti99.imagetools.io.Cluster;
import com.miriki.ti99.imagetools.io.DataChainBlockIO;
import com.miriki.ti99.imagetools.io.Sector;

/**
 * High-level cluster management for TI-99 disk images.
 *
 * Responsibilities:
 *   - allocate clusters (contiguous groups of sectors)
 *   - write data into clusters
 *   - free clusters based on FDR data chains
 *
 * This class operates on the AllocationBitmap and DiskFormat to ensure
 * correct mapping between clusters and physical sectors.
 */
public final class ClusterService {

    private static final Logger log = LoggerFactory.getLogger(ClusterService.class);

    private ClusterService() {
        // Utility class – no instances
    }

    // ============================================================
    //  PUBLIC API – ALLOCATE CLUSTERS
    // ============================================================

    /**
     * Allocates the requested number of clusters.
     *
     * A cluster is free only if *all* sectors belonging to it are free.
     *
     * @param fs             the TI-99 filesystem context
     * @param neededClusters number of clusters to allocate
     * @return list of allocated cluster indices
     */
    public static List<Integer> allocateClusters(Ti99FileSystem fs, int neededClusters) {
        log.debug("allocateClusters(fs={}, needed={})", fs, neededClusters);

        AllocationBitmap abm = requireAbm(fs);
        DiskFormat fmt = fs.getImage().getFormat();

        List<Integer> allocated = new ArrayList<>();
        int totalClusters = fmt.getClusterCount();

        for (int cluster = 0;
             cluster < totalClusters && allocated.size() < neededClusters;
             cluster++) {

            if (isClusterFree(abm, fmt, cluster)) {
                markClusterUsed(abm, fmt, cluster, true);
                allocated.add(cluster);
            }
        }

        if (allocated.size() != neededClusters) {
            throw new IllegalStateException("Not enough free clusters");
        }

        return allocated;
    }

    // ============================================================
    //  PUBLIC API – WRITE DATA INTO CLUSTERS
    // ============================================================

    /**
     * Writes raw file data into the given clusters.
     *
     * Data is written sector-by-sector, cluster-by-cluster.
     * Remaining space is filled with 0x00.
     */
    public static void writeDataToClusters(Ti99FileSystem fs, List<Integer> clusters, byte[] data) {
        log.debug("writeDataToClusters(fs={}, clusters={}, dataLen={})",
                fs, clusters, data.length);

        Ti99Image img = fs.getImage();
        DiskFormat fmt = img.getFormat();

        int sectorsPerCluster = fmt.getSectorsPerCluster();
        int sectorSize = Sector.SIZE;

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

    /**
     * Frees all clusters referenced by the FDR's HFDC Data Chain Pointer (DCP).
     */
    public static void freeClusters(Ti99FileSystem fs, FileDescriptorRecord fdr) {
        log.debug("freeClusters(fs={}, fdr={})", fs, fdr);

        AllocationBitmap abm = requireAbm(fs);
        DiskFormat fmt = fs.getImage().getFormat();

        int fdrSector = findFdrSector(fs, fdr);

        // Read cluster list from DCP block
        List<Integer> oldClusters =
                DataChainBlockIO.readClusterChain(fs.getImage(), fdrSector);

        for (int cluster : oldClusters) {
            markClusterUsed(abm, fmt, cluster, false);
        }
    }

    // ============================================================
    //  INTERNAL HELPERS – ABM / FORMAT
    // ============================================================

    private static AllocationBitmap requireAbm(Ti99FileSystem fs) {
        AllocationBitmap abm = fs.getAbm();
        if (abm == null) {
            throw new IllegalStateException("ABM not set");
        }
        return abm;
    }

    /**
     * Returns true if *all* sectors of the cluster are free.
     */
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

    /**
     * Marks all sectors of a cluster as used or free.
     */
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

    /**
     * Writes up to sectorSize bytes into the sector.
     * Remaining bytes are filled with 0x00.
     *
     * @return new data offset
     */
    private static int writeSector(Sector sector, byte[] data, int pos, int sectorSize) {
        for (int i = 0; i < sectorSize; i++) {
            byte value = (pos < data.length) ? data[pos++] : 0x00;
            sector.set(i, value);
        }
        return pos;
    }

    // ============================================================
    //  INTERNAL HELPERS – FDR LOOKUP
    // ============================================================

    /**
     * Finds the FDR sector index by locating the FDR in the filesystem's list.
     *
     * Note:
     *   fs.getFiles() must be aligned with FDR sector ordering.
     */
    private static int findFdrSector(Ti99FileSystem fs, FileDescriptorRecord fdr) {
        int index = fs.getFiles().indexOf(fdr);
        if (index < 0) {
            throw new IllegalArgumentException("FDR not found");
        }
        return index;
    }
}
