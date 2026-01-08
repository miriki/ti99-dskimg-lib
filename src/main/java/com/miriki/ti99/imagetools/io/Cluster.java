package com.miriki.ti99.imagetools.io;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.miriki.ti99.imagetools.domain.DiskFormat;

/**
 * Represents a logical cluster inside a TI-99 disk image.
 *
 * A cluster is a group of consecutive sectors. The number of sectors per
 * cluster depends on the DiskFormat (e.g. HFDC, TI-Controller, RAMdisk).
 *
 * This class provides:
 *   - cluster index validation
 *   - mapping cluster → first sector
 *   - access to individual sectors inside the cluster
 *
 * It does NOT copy data; it only provides a view into the underlying image.
 */
public final class Cluster {

    private static final Logger log = LoggerFactory.getLogger(Cluster.class);

    private final DiskFormat format;   // describes geometry + cluster layout
    private final byte[] imageBytes;   // full disk image
    private final int clusterIndex;    // 0-based cluster number

    // ============================================================
    //  CONSTRUCTOR
    // ============================================================

    /**
     * Creates a logical cluster view.
     *
     * @param format        disk format describing cluster geometry
     * @param imageBytes    full disk image byte array
     * @param clusterIndex  index of the cluster (0-based)
     */
    public Cluster(DiskFormat format, byte[] imageBytes, int clusterIndex) {
        log.debug("[constructor] Cluster(format={}, imageBytes={}, index={})",
                format, imageBytes, clusterIndex);

        this.format = Objects.requireNonNull(format, "DiskFormat must not be null");
        this.imageBytes = Objects.requireNonNull(imageBytes, "Image bytes must not be null");

        if (clusterIndex < 0 || clusterIndex >= format.getClusterCount()) {
            throw new IndexOutOfBoundsException(
                    "clusterIndex " + clusterIndex +
                    " out of range 0.." + (format.getClusterCount() - 1)
            );
        }

        this.clusterIndex = clusterIndex;
    }

    // ============================================================
    //  BASIC PROPERTIES
    // ============================================================

    /**
     * Returns the cluster index (0-based).
     */
    public int getIndex() {
        log.debug("getIndex()");
        return clusterIndex;
    }

    /**
     * Returns the sector index of the first sector belonging to this cluster.
     *
     * Important:
     *   Cluster 0 does NOT start at sector 0.
     *   It starts at the format-defined first data sector.
     */
    public int getFirstSectorIndex() {
        log.debug("getFirstSectorIndex()");
        return format.clusterToSector(clusterIndex);
    }

    /**
     * Returns the number of sectors in this cluster.
     */
    public int getTotalSectors() {
        log.debug("getTotalSectors()");
        return format.getSectorsPerCluster();
    }

    // ============================================================
    //  SECTOR ACCESS
    // ============================================================

    /**
     * Returns a Sector object for the given offset inside the cluster.
     *
     * @param offsetInCluster 0-based offset inside the cluster
     * @return Sector view
     */
    public Sector getSector(int offsetInCluster) {
        log.debug("getSector(offset={})", offsetInCluster);

        if (offsetInCluster < 0 || offsetInCluster >= getTotalSectors()) {
            throw new IllegalArgumentException(
                    "Offset " + offsetInCluster +
                    " out of bounds for cluster size " + getTotalSectors()
            );
        }

        int sectorIndex = getFirstSectorIndex() + offsetInCluster;
        return new Sector(imageBytes, sectorIndex);
    }
}
