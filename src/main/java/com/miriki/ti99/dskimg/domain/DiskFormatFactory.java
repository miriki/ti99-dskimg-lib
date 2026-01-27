package com.miriki.ti99.dskimg.domain;

import java.util.Objects;

import com.miriki.ti99.dskimg.domain.enums.DiskDensity;

/**
 * Factory for constructing DiskFormat instances from high-level parameters.
 *
 * Responsibilities:
 *  - compute physical geometry (tracks, sides, sectors)
 *  - compute logical layout (VIB, FDI, FDR zone, data zone)
 *  - compute cluster configuration
 *  - provide helpers for common TI-99 layouts
 */
public final class DiskFormatFactory {

    private DiskFormatFactory() {}

    // ============================================================
    //  CONSTANTS – TI-DOS LAYOUT
    // ============================================================

    private static final int VIB_SECTOR = 0;
    private static final int FDI_SECTOR = 1;
    private static final int FIRST_FDR_SECTOR = 2;

    // ============================================================
    //  PUBLIC API – GENERIC FORMAT BUILDER
    // ============================================================

    public static DiskFormat create(
            int tracksPerSide,
            int sides,
            int sectorsPerTrack,
            DiskDensity density,
            int fdrSectorCount,
            int reservedSectors,
            int sectorsPerCluster) {

        Objects.requireNonNull(density, "density must not be null");

        if (tracksPerSide <= 0) throw new IllegalArgumentException("tracksPerSide must be > 0");
        if (sides <= 0) throw new IllegalArgumentException("sides must be > 0");
        if (sectorsPerTrack <= 0) throw new IllegalArgumentException("sectorsPerTrack must be > 0");
        if (fdrSectorCount < 0) throw new IllegalArgumentException("fdrSectorCount must be >= 0");
        if (reservedSectors < 0) throw new IllegalArgumentException("reservedSectors must be >= 0");
        if (sectorsPerCluster <= 0) throw new IllegalArgumentException("sectorsPerCluster must be > 0");

        int totalSectors = tracksPerSide * sides * sectorsPerTrack;

        int firstDataSector = FIRST_FDR_SECTOR + fdrSectorCount + reservedSectors;

        return new DiskFormat(
                totalSectors,
                sectorsPerTrack,
                tracksPerSide,
                sides,
                density,
                VIB_SECTOR,
                FDI_SECTOR,
                FIRST_FDR_SECTOR,
                fdrSectorCount,
                firstDataSector,
                sectorsPerCluster
        );
    }

    // ============================================================
    //  PRESETS
    // ============================================================

    public static DiskFormat createSssd() {
        return create(40, 1, 9, DiskDensity.SD, 32, 0, 1);
    }

    public static DiskFormat createDsDd() {
        return create(40, 2, 18, DiskDensity.DD, 32, 0, 1);
    }

    public static DiskFormat createHfdc80() {
        return create(80, 2, 18, DiskDensity.DD, 32, 0, 4);
    }

    // ============================================================
    //  UI HELPERS
    // ============================================================

    public static String describe(DiskFormat fmt) {
        Objects.requireNonNull(fmt, "fmt must not be null");

        return String.format(
                "%d tracks × %d sides × %d sectors/track = %d sectors, cluster=%d",
                fmt.getTracksPerSide(),
                fmt.getSides(),
                fmt.getSectorsPerTrack(),
                fmt.getTotalSectors(),
                fmt.getSectorsPerCluster()
        );
    }
}
