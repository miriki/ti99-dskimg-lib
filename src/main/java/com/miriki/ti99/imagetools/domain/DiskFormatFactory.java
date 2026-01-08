package com.miriki.ti99.imagetools.domain;

import java.util.Objects;

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
    //  PUBLIC API – GENERIC FORMAT BUILDER
    // ============================================================

    /**
     * Creates a DiskFormat from high-level geometry and layout parameters.
     *
     * @param tracksPerSide     number of tracks per side (e.g. 40)
     * @param sides             number of sides (1 = SS, 2 = DS)
     * @param sectorsPerTrack   sectors per track (e.g. 9)
     * @param density           SINGLE, DOUBLE, QUAD
     * @param fdrSectorCount    number of FDR sectors (TI DOS: 14)
     * @param reservedSectors   number of reserved sectors after FDR zone
     * @param sectorsPerCluster cluster size (1 = SSSD, 2/4 = DD/QD)
     */
    public static DiskFormat create(
            int tracksPerSide,
            int sides,
            int sectorsPerTrack,
            DiskFormat.Density density,
            int fdrSectorCount,
            int reservedSectors,
            int sectorsPerCluster) {

        Objects.requireNonNull(density);

        int totalSectors = tracksPerSide * sides * sectorsPerTrack;

        int vibSector = 0;
        int fdiSector = 1;
        int firstFdrSector = 2;

        int firstDataSector = firstFdrSector + fdrSectorCount + reservedSectors;

        return new DiskFormat(
                totalSectors,
                sectorsPerTrack,
                tracksPerSide,
                sides,
                density,
                vibSector,
                fdiSector,
                firstFdrSector,
                fdrSectorCount,
                firstDataSector,
                sectorsPerCluster
        );
    }

    // ============================================================
    //  PRESETS – BUILT USING THE GENERIC FACTORY
    // ============================================================

    /** TIImageTool-compatible SSSD (40 tracks, 1 side, 9 sectors/track) */
    public static DiskFormat createSssd() {
        return create(
                40,                     // tracks per side
                1,                      // sides
                9,                      // sectors per track
                DiskFormat.Density.SINGLE,
                32,                     // FDR sectors (2..15)
                0,                     // reserved (16..33)
                1                       // sectors per cluster
        );
    }

    /** Double-Sided Double-Density (example) */
    public static DiskFormat createDsDd() {
        return create(
                40,
                2,
                9,
                DiskFormat.Density.DOUBLE,
                32,
                0,
                1                       // cluster = 2 sectors
        );
    }

    /** HFDC 80-track DD (example placeholder) */
    public static DiskFormat createHfdc80() {
        return create(
                80,
                2,
                18,
                DiskFormat.Density.DOUBLE,
                32,                     // more FDRs
                0,                     // more reserved
                4                       // cluster = 4 sectors
        );
    }

    // ============================================================
    //  UI HELPERS
    // ============================================================

    public static String describe(DiskFormat fmt) {
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
