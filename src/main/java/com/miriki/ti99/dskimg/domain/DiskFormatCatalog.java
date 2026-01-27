package com.miriki.ti99.dskimg.domain;

import java.util.*;

/**
 * Central registry of all known disk format presets.
 *
 * Pure metadata:
 *   - no IO logic
 *   - no disk access
 *   - no dynamic computation
 *
 * Presets are registered once at class load time and remain available
 * for the lifetime of the application.
 */
public final class DiskFormatCatalog {

    /** Registry of presets by name (in insertion order). */
    private static final Map<String, DiskFormatPreset> PRESETS = new LinkedHashMap<>();

    // ============================================================
    //  STATIC INITIALIZATION – BUILT-IN PRESETS
    // ============================================================

    static {
        register(DiskFormatPreset.TI_SSSD);
        register(DiskFormatPreset.TI_DSSD);
        register(DiskFormatPreset.TI_DSDD);
    }

    private DiskFormatCatalog() {
        // Utility class – no instances
    }

    // ============================================================
    //  REGISTRATION
    // ============================================================

    /**
     * Registers a disk format preset.
     * Replaces any existing preset with the same name.
     */
    public static void register(DiskFormatPreset preset) {
        Objects.requireNonNull(preset, "preset must not be null");
        PRESETS.put(preset.getName(), preset);
    }

    // ============================================================
    //  LOOKUP
    // ============================================================

    /**
     * Returns the preset with the given name, or an empty Optional if not found.
     */
    public static Optional<DiskFormatPreset> getPreset(String name) {
        return Optional.ofNullable(PRESETS.get(name));
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
        return List.copyOf(PRESETS.keySet());
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
        DiskFormatPreset preset = PRESETS.get(name);
        if (preset == null) {
            throw new IllegalArgumentException("Unknown disk format preset: " + name);
        }
        return preset.getFormat();
    }

    @Override
    public String toString() {
        return "DiskFormatCatalog{" +
                "presets=" + PRESETS.keySet() +
                '}';
    }
}
