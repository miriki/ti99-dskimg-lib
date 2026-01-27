package com.miriki.ti99.dskimg.domain;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.miriki.ti99.dskimg.domain.constants.DiskGeometryConstants;
import com.miriki.ti99.dskimg.domain.constants.FdiConstants;
import com.miriki.ti99.dskimg.domain.constants.FdrConstants;
import com.miriki.ti99.dskimg.domain.constants.VibConstants;
import com.miriki.ti99.dskimg.domain.enums.DiskDensity;
import com.miriki.ti99.dskimg.domain.enums.DiskSides;

/**
 * Logical disk format description, HFDC-compatible.
 *
 * Encodes both physical geometry (tracks, sides, sectors) and
 * filesystem layout (VIB, FDI, FDRs, data area)
 * plus cluster configuration.
 */
public final class DiskFormat {

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(DiskFormat.class);

    // ============================================================
    //  PHYSICAL GEOMETRY
    // ============================================================

    private final int totalSectors;
    private final int sectorsPerTrack;
    private final int tracksPerSide;
    private final int sides;
    private final DiskDensity density;

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

    // ============================================================
    //  CONSTRUCTOR
    // ============================================================

    public DiskFormat(
            int totalSectors,
            int sectorsPerTrack,
            int tracksPerSide,
            int sides,
            DiskDensity density,
            int vibSector,
            int fdiSector,
            int firstFdrSector,
            int fdrSectorCount,
            int firstDataSector,
            int sectorsPerCluster) {

        requirePositive(totalSectors, "totalSectors");
        requirePositive(sectorsPerTrack, "sectorsPerTrack");
        requirePositive(tracksPerSide, "tracksPerSide");
        requirePositive(sides, "sides");
        requirePositive(sectorsPerCluster, "sectorsPerCluster");
        requirePositive(fdrSectorCount, "fdrSectorCount");
        requirePositive(firstFdrSector, "firstFdrSector");
        requirePositive(firstDataSector, "firstDataSector");

        if (firstDataSector <= firstFdrSector) {
            throw new IllegalArgumentException("firstDataSector must be > firstFdrSector");
        }

        if (firstDataSector >= totalSectors) {
            throw new IllegalArgumentException("firstDataSector must be < totalSectors");
        }

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
    public DiskDensity getDensity()   { return density; }

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
    //  CONVENIENCE METHODS
    // ============================================================

    /** Last sector of the FDR zone. */
    public int getLastFdrSector() {
        return firstFdrSector + fdrSectorCount - 1;
    }

    /** Number of data sectors. */
    public int getDataSectorCount() {
        return totalSectors - firstDataSector;
    }

    /** Last sector of the data area. */
    public int getLastDataSector() {
        return totalSectors - 1;
    }

    /** True if the given sector is inside the FDR zone. */
    public boolean isFdrSector(int sector) {
        return sector >= firstFdrSector && sector <= getLastFdrSector();
    }

    /** True if the given sector is inside the data area. */
    public boolean isDataSector(int sector) {
        return sector >= firstDataSector && sector < totalSectors;
    }

    // ============================================================
    //  PRESET (kept for convenience, but ideally moved to Factory)
    // ============================================================

    /**
     * TIImageTool-compatible SSSD format:
     *  - 40 tracks, 1 side, 9 sectors/track = 360 sectors
     *  - VIB = 0, FDI = 1
     *  - FDR zone = sectors 2..33 (32 sectors)
     *  - data area starts at sector 34
     *  - sectorsPerCluster = 1
     */
    public static DiskFormat createStandardSssd() {

        int tracksPerSide = DiskGeometryConstants.TRACKS_PER_SIDE_SSSD;
        int sides = DiskSides.SS.getCode();
        int sectorsPerTrack = DiskGeometryConstants.SECTORS_PER_TRACK_SSSD;

        int totalSectors = tracksPerSide * sides * sectorsPerTrack;

        return new DiskFormat(
                totalSectors,
                sectorsPerTrack,
                tracksPerSide,
                sides,
                DiskDensity.SD,
                VibConstants.VIB_SECTOR,
                FdiConstants.FDI_SECTOR,
                FdrConstants.FDR_FIRST_SECTOR_SSSD,
                FdrConstants.FDR_SECTOR_COUNT_SSSD,
                FdrConstants.FIRST_DATA_SECTOR_SSSD,
                1 // sectorsPerCluster
        );
    }
}
