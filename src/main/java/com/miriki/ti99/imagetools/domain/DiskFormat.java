package com.miriki.ti99.imagetools.domain;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logical disk format description, HFDC-compatible.
 *
 * Encodes both physical geometry (tracks, sides, sectors) and
 * filesystem layout (VIB, FDI, FDRs, data area)
 * plus cluster configuration.
 */
public final class DiskFormat {

    private static final Logger log = LoggerFactory.getLogger(DiskFormat.class);

    // ============================================================
    //  PHYSICAL GEOMETRY
    // ============================================================

    private final int totalSectors;
    private final int sectorsPerTrack;
    private final int tracksPerSide;
    private final int sides;
    private final Density density;

    // ============================================================
    //  LOGICAL LAYOUT (HFDC / TI DOS)
    // ============================================================

    private final int vibSector;        // usually 0
    private final int fdiSector;        // usually 1
    private final int firstFdrSector;   // usually 2
    private final int fdrSectorCount;   // number of FDR sectors
    private final int firstDataSector;  // start of data area

    // ============================================================
    //  CLUSTER CONFIGURATION
    // ============================================================

    private final int sectorsPerCluster;

    public enum Density {
        SINGLE,   // SD
        DOUBLE,   // DD
        QUAD      // QD
    }

    // ============================================================
    //  CONSTRUCTOR
    // ============================================================

    public DiskFormat(
            int totalSectors,
            int sectorsPerTrack,
            int tracksPerSide,
            int sides,
            Density density,
            int vibSector,
            int fdiSector,
            int firstFdrSector,
            int fdrSectorCount,
            int firstDataSector,
            int sectorsPerCluster) {

        // log.debug("[constructor] DiskFormat({}, ... {})", totalSectors, sectorsPerCluster);

        requirePositive(totalSectors, "totalSectors");
        requirePositive(sectorsPerTrack, "sectorsPerTrack");
        requirePositive(tracksPerSide, "tracksPerSide");
        requirePositive(sides, "sides");
        requirePositive(sectorsPerCluster, "sectorsPerCluster");

        this.totalSectors = totalSectors;
        this.sectorsPerTrack = sectorsPerTrack;
        this.tracksPerSide = tracksPerSide;
        this.sides = sides;
        this.density = Objects.requireNonNull(density);

        this.vibSector = vibSector;
        this.fdiSector = fdiSector;
        this.firstFdrSector = firstFdrSector;
        this.fdrSectorCount = fdrSectorCount;
        this.firstDataSector = firstDataSector;
        this.sectorsPerCluster = sectorsPerCluster;
    }

    private static void requirePositive(int value, String name) {
        if (value <= 0) throw new IllegalArgumentException(name + " must be > 0");
    }

    // ============================================================
    //  PHYSICAL GEOMETRY ACCESSORS
    // ============================================================

    public int getTotalSectors()      { return totalSectors; }
    public int getSectorsPerTrack()   { return sectorsPerTrack; }
    public int getTracksPerSide()     { return tracksPerSide; }
    public int getSides()             { return sides; }
    public Density getDensity()       { return density; }

    // ============================================================
    //  LOGICAL LAYOUT ACCESSORS
    // ============================================================

    public int getVibSector()         { return vibSector; }
    public int getFdiSector()         { return fdiSector; }
    public int getFirstFdrSector()    { return firstFdrSector; }
    public int getFdrSectorCount()    { return fdrSectorCount; }
    public int getFirstDataSector()   { return firstDataSector; }

    // ============================================================
    //  CLUSTER CONFIGURATION
    // ============================================================

    public int getSectorsPerCluster() { return sectorsPerCluster; }

    /** Number of addressable clusters in the data area. */
    public int getClusterCount() {
        int dataSectors = totalSectors - firstDataSector;
        return Math.max(0, dataSectors / sectorsPerCluster);
    }

    /** Translate a cluster index (0-based) to the corresponding first sector. */
    public int clusterToSector(int clusterIndex) {
        int max = getClusterCount();
        if (clusterIndex < 0 || clusterIndex >= max) {
            throw new IndexOutOfBoundsException(
                "clusterIndex " + clusterIndex + " out of range 0.." + (max - 1)
            );
        }
        return firstDataSector + clusterIndex * sectorsPerCluster;
    }

    // ============================================================
    //  PRESETS
    // ============================================================

    /**
     * TIImageTool-compatible SSSD format:
     *  - 40 tracks, 1 side, 9 sectors/track = 360 sectors
     *  - VIB = 0, FDI = 1
     *  - FDRs = sectors 2..15 (14 sectors)
     *  - reserved = 16..33
     *  - data area starts at sector 34
     *  - sectorsPerCluster = 1
     */
    public static DiskFormat createStandardSssd() {
        int tracksPerSide = 40;
        int sides = 1;
        int sectorsPerTrack = 9;
        int totalSectors = tracksPerSide * sides * sectorsPerTrack; // 360

        int vibSector = 0;
        int fdiSector = 1;
        int firstFdrSector = 2;
        int fdrSectorCount = 14;   // sectors 2..15 inclusive
        int firstDataSector = 34;
        int sectorsPerCluster = 1;

        return new DiskFormat(
                totalSectors,
                sectorsPerTrack,
                tracksPerSide,
                sides,
                Density.SINGLE,
                vibSector,
                fdiSector,
                firstFdrSector,
                fdrSectorCount,
                firstDataSector,
                sectorsPerCluster
        );
    }
}
