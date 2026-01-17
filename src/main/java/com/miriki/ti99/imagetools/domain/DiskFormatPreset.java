package com.miriki.ti99.imagetools.domain;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A named preset for a disk format.
 *
 * This class contains:
 *   - a human-readable name
 *   - a description
 *   - a factory that produces a DiskFormat instance
 *
 * Presets are pure metadata and contain no IO logic.
 * They are used by DiskFormatCatalog and by UI components
 * to present selectable disk geometries.
 */
public final class DiskFormatPreset {

    @SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(DiskFormatPreset.class);

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

    /**
     * Creates a new disk format preset.
     *
     * @param name        preset name
     * @param description human-readable description
     * @param factory     factory that creates the DiskFormat
     */
    public DiskFormatPreset(String name, String description, DiskFormatFactory factory) {
        // log.debug("[constructor] DiskFormatPreset({}, {}, {})", name, description, factory);

        this.name = Objects.requireNonNull(name, "name must not be null");
        this.description = Objects.requireNonNull(description, "description must not be null");
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

    /**
     * Creates a new DiskFormat instance from this preset.
     */
    public DiskFormat getFormat() {
        return factory.create();
    }

    // ============================================================
    //  BUILT-IN PRESETS (TIImageTool-compatible)
    // ============================================================

    /**
     * TIImageTool‑compatible SSSD (40 tracks, 1 side, 9 sectors).
     */
    public static final DiskFormatPreset TI_SSSD =
            new DiskFormatPreset(
                    "TI SSSD 40-Track",
                    "Single-sided, single-density, 40 tracks, 9 sectors/track (360 sectors)",
                    DiskFormat::createStandardSssd
            );

    /**
     * TIImageTool‑compatible DSSD (40 tracks, 2 sides, 9 sectors).
     */
    public static final DiskFormatPreset TI_DSSD =
            new DiskFormatPreset(
                    "TI DSSD 40-Track",
                    "Double-sided, single-density, 40 tracks, 9 sectors/track (720 sectors)",
                    () -> new DiskFormat(
                            40 * 2 * 9,   // total sectors
                            9,            // sectors per track
                            40,           // tracks
                            2,            // sides
                            DiskFormat.Density.SINGLE,
                            0,            // VIB sector
                            1,            // FDI sector
                            2,            // FDR start
                            32,           // FDR count
                            34,           // data start
                            1             // sectors per cluster
                    )
            );

    /**
     * TIImageTool‑compatible DS/DD (40 tracks, 2 sides, 18 sectors).
     */
    public static final DiskFormatPreset TI_DSDD =
            new DiskFormatPreset(
                    "TI DSDD 40-Track",
                    "Double-sided, double-density, 40 tracks, 18 sectors/track (1440 sectors)",
                    () -> new DiskFormat(
                            40 * 2 * 18,
                            18,
                            40,
                            2,
                            DiskFormat.Density.DOUBLE,
                            0,
                            1,
                            2,
                            32,
                            34,
                            1
                    )
            );

    // ============================================================
    //  PRESET LISTING
    // ============================================================

    private static final DiskFormatPreset[] ALL = {
            TI_SSSD,
            TI_DSSD,
            TI_DSDD
    };

    /**
     * Returns all built-in presets.
     */
    public static DiskFormatPreset[] allPresets() {
        return ALL.clone();
    }
}
