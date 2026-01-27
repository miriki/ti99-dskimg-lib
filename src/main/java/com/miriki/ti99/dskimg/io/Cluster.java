package com.miriki.ti99.dskimg.io;

import java.util.Objects;

import com.miriki.ti99.dskimg.domain.DiskFormat;

/**
 * Represents a logical cluster inside a TI‑99 disk image.
 *
 * A cluster is a group of consecutive sectors. The number of sectors per
 * cluster depends on the DiskFormat. This class provides a view into the
 * underlying image without copying data.
 */
public final class Cluster {

    private final DiskFormat format;
    private final byte[] imageBytes;
    private final int clusterIndex;

    // ============================================================
    //  CONSTRUCTOR
    // ============================================================

    public Cluster(DiskFormat format, byte[] imageBytes, int clusterIndex) {
        this.format = Objects.requireNonNull(format, "format must not be null");
        this.imageBytes = Objects.requireNonNull(imageBytes, "imageBytes must not be null");

        int max = format.getClusterCount();
        if (clusterIndex < 0 || clusterIndex >= max) {
            throw new IndexOutOfBoundsException(
                    "clusterIndex " + clusterIndex + " out of range 0.." + (max - 1)
            );
        }

        this.clusterIndex = clusterIndex;
    }

    // ============================================================
    //  BASIC PROPERTIES
    // ============================================================

    /** Returns the cluster index (0-based). */
    public int getIndex() {
        return clusterIndex;
    }

    /**
     * Returns the sector index of the first sector belonging to this cluster.
     * Cluster 0 starts at the format-defined first data sector.
     */
    public int getFirstSectorIndex() {
        return format.clusterToSector(clusterIndex);
    }

    /** Returns the number of sectors in this cluster. */
    public int getTotalSectors() {
        return format.getSectorsPerCluster();
    }

    // ============================================================
    //  SECTOR ACCESS
    // ============================================================

    /**
     * Returns a Sector view for the given offset inside the cluster.
     *
     * @param offsetInCluster 0-based offset inside the cluster
     */
    public Sector getSector(int offsetInCluster) {
        int total = getTotalSectors();

        if (offsetInCluster < 0 || offsetInCluster >= total) {
            throw new IndexOutOfBoundsException(
                    "Offset " + offsetInCluster + " out of bounds for cluster size " + total
            );
        }

        int sectorIndex = getFirstSectorIndex() + offsetInCluster;
        return new Sector(imageBytes, sectorIndex);
    }
}
