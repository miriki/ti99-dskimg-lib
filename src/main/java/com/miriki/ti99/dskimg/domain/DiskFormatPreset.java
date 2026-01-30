package com.miriki.ti99.dskimg.domain;

import java.util.List;
import java.util.Objects;

import com.miriki.ti99.dskimg.domain.enums.DiskDensity;

/**
 * A named preset for a disk format.
 *
 * Pure metadata:
 *   - name
 *   - description
 *   - factory producing a DiskFormat instance
 */
public final class DiskFormatPreset {

    /** Display name of the preset (e.g. "TI SSSD 40-Track"). */
    private final String name;

    /** Human-readable description of the geometry. */
    private final String description;

    /** Factory that produces a DiskFormat instance on demand. */
    private final DiskFormatFactory factory;

    // ============================================================
    //  FACTORY INTERFACE
    // ============================================================

    @FunctionalInterface
    public interface DiskFormatFactory {
        DiskFormat create();
    }

    // ============================================================
    //  CONSTRUCTOR
    // ============================================================

    public DiskFormatPreset(String name, String description, DiskFormatFactory factory) {
        this.name = Objects.requireNonNull(name, "name must not be null").trim();
        this.description = Objects.requireNonNull(description, "description must not be null").trim();
        this.factory = Objects.requireNonNull(factory, "factory must not be null");
    }

    // ============================================================
    //  ACCESSORS
    // ============================================================

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    /** Creates a new DiskFormat instance from this preset. */
    public DiskFormat getFormat() {
        return factory.create();
    }

    public int getDiskSize() {
        return getFormat().getDiskSize();
    }

    // ============================================================
    //  BUILT-IN PRESETS (TIImageTool-compatible)
    // ============================================================

    public static final DiskFormatPreset TI_SSSD =
            new DiskFormatPreset(
                    "TI SSSD 40-Track",
                    "Single-sided, single-density, 40 tracks, 9 sectors/track (360 sectors)",
                    DiskFormat::createStandardSssd
            );

    public static final DiskFormatPreset TI_DSSD =
            new DiskFormatPreset(
                    "TI DSSD 40-Track",
                    "Double-sided, single-density, 40 tracks, 9 sectors/track (720 sectors)",
                    () -> new DiskFormat(
                            40 * 2 * 9,
                            9,
                            40,
                            2,
                            DiskDensity.SD,
                            0,
                            1,
                            2,
                            32,
                            34,
                            1
                    )
            );

    public static final DiskFormatPreset TI_DSDD =
            new DiskFormatPreset(
                    "TI DSDD 40-Track",
                    "Double-sided, double-density, 40 tracks, 18 sectors/track (1440 sectors)",
                    () -> new DiskFormat(
                            40 * 2 * 18,
                            18,
                            40,
                            2,
                            DiskDensity.DD,
                            0,
                            1,
                            2,
                            32,
                            34,
                            1
                    )
            );

    public static final DiskFormatPreset TI_DSSD_80 =
            new DiskFormatPreset(
                    "TI DSSD 80-Track",
                    "Double-sided, single-density, 80 tracks, 9 sectors/track (1440 sectors)",
                    () -> new DiskFormat(
                            80 * 2 * 9,
                            9,
                            80,
                            2,
                            DiskDensity.SD,
                            0,
                            1,
                            2,
                            32,
                            34,
                            1
                    )
            );

    public static final DiskFormatPreset TI_DSDD_80 =
            new DiskFormatPreset(
                    "TI DSDD 80-Track",
                    "Double-sided, double-density, 80 tracks, 18 sectors/track (2880 sectors)",
                    () -> new DiskFormat(
                            80 * 2 * 18,
                            18,
                            80,
                            2,
                            DiskDensity.DD,
                            0,
                            1,
                            2,
                            32,
                            34,
                            4
                    )
            );

    // ============================================================
    //  PRESET LISTING
    // ============================================================

    private static final List<DiskFormatPreset> ALL = List.of(
            TI_SSSD,
            TI_DSSD,
            TI_DSDD,
            TI_DSSD_80,
            TI_DSDD_80
    );

    /** Returns all built-in presets. */
    public static List<DiskFormatPreset> allPresets() {
        return ALL;
    }

    // ============================================================
    //  DEBUG STRING
    // ============================================================

    @Override
    public String toString() {
        return "DiskFormatPreset{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
