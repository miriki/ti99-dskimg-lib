package com.miriki.ti99.imagetools.domain;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central registry of all known disk format presets.
 *
 * This catalog contains pure metadata:
 *   - no IO logic
 *   - no disk access
 *   - no dynamic computation
 *
 * Presets are registered once at class load time and remain immutable
 * for the lifetime of the application. Additional presets may be added
 * programmatically via {@link #register(DiskFormatPreset)}.
 */
public final class DiskFormatCatalog {

    private static final Logger log = LoggerFactory.getLogger(DiskFormatCatalog.class);

    /**
     * Registry of presets by name.
     * LinkedHashMap preserves insertion order for predictable listing.
     */
    private static final Map<String, DiskFormatPreset> PRESETS = new LinkedHashMap<>();

    // ============================================================
    //  STATIC INITIALIZATION – BUILT-IN PRESETS
    // ============================================================

    static {
        // TIImageTool-compatible defaults
        register(DiskFormatPreset.TI_SSSD);
        register(DiskFormatPreset.TI_DSSD);
        register(DiskFormatPreset.TI_DSDD);

        // Additional formats may be added here in the future:
        // register(DiskFormatPreset.HFDC_80_TRACK);
        // register(DiskFormatPreset.QD_360K);
        // register(DiskFormatPreset.HD_1440K);
    }

    private DiskFormatCatalog() {
        // Utility class – no instances
    }

    // ============================================================
    //  REGISTRATION
    // ============================================================

    /**
     * Registers a disk format preset.
     *
     * If a preset with the same name already exists, it is replaced.
     */
    public static void register(DiskFormatPreset preset) {
        log.debug("register({})", preset);

        Objects.requireNonNull(preset, "preset must not be null");
        PRESETS.put(preset.getName(), preset);
    }

    // ============================================================
    //  LOOKUP
    // ============================================================

    /**
     * Returns the preset with the given name, or null if not found.
     */
    public static DiskFormatPreset getPreset(String name) {
        log.debug("getPreset({})", name);
        return PRESETS.get(name);
    }

    /**
     * Returns an immutable collection of all registered presets.
     */
    public static Collection<DiskFormatPreset> getAllPresets() {
        return Collections.unmodifiableCollection(PRESETS.values());
    }

    /**
     * Returns all preset names in registration order.
     */
    public static List<String> getPresetNames() {
        return new ArrayList<>(PRESETS.keySet());
    }

    // ============================================================
    //  CONVENIENCE
    // ============================================================

    /**
     * Returns the DiskFormat for the given preset name.
     *
     * @throws IllegalArgumentException if the preset does not exist
     */
    public static DiskFormat getFormat(String name) {
        log.debug("getFormat({})", name);

        DiskFormatPreset preset = PRESETS.get(name);
        if (preset == null) {
            throw new IllegalArgumentException("Unknown disk format preset: " + name);
        }
        return preset.getFormat();
    }
}
