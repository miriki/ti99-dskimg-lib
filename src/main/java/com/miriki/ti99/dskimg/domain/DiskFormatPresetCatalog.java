package com.miriki.ti99.dskimg.domain;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.miriki.ti99.dskimg.domain.enums.DiskDensity;

/**
 * Central registry of known TI‑99 disk format presets.
 *
 * Pure metadata:
 *   - lookup by name
 *   - enumeration for UI components
 *   - registration of custom formats
 *
 * Preserves insertion order for predictable UI listing.
 */
public final class DiskFormatPresetCatalog {

    // ============================================================
    //  ENTRY TYPE
    // ============================================================

    /**
     * Immutable catalog entry: name + description + DiskFormat.
     */
    public static final class Entry {
        private final String name;
        private final String description;
        private final DiskFormat format;

        public Entry(String name, String description, DiskFormat format) {
            this.name = Objects.requireNonNull(name, "name must not be null").trim();
            this.description = Objects.requireNonNull(description, "description must not be null").trim();
            this.format = Objects.requireNonNull(format, "format must not be null");
        }

        public String getName()        { return name; }
        public String getDescription() { return description; }
        public DiskFormat getFormat()  { return format; }

        @Override
        public String toString() {
            return name;
        }
    }

    // ============================================================
    //  INTERNAL REGISTRY
    // ============================================================

    /** Registry of presets by name (in insertion order). */
    private static final Map<String, Entry> PRESETS = new LinkedHashMap<>();

    private DiskFormatPresetCatalog() {
        // Utility class – no instances
    }

    // ============================================================
    //  STATIC INITIALIZATION – BUILT-IN PRESETS
    // ============================================================

    static {
        // TI FDC (SSSD)
        registerFactoryPreset("SSSD (TI FDC)", DiskFormatFactory.createSssd());

        // CorComp
        registerFactoryPreset("DSSD (CorComp)",
                DiskFormatFactory.create(40, 2, 9, DiskDensity.SD, 32, 0, 1));

        registerFactoryPreset("DSDD (CorComp)", DiskFormatFactory.createDsDd());

        // TI DDCC (Double Density)
        registerFactoryPreset("DSDD (TI DDCC)", DiskFormatFactory.createDsDd());

        // BwG Controller
        registerFactoryPreset("DSSD (BwG)",
                DiskFormatFactory.create(40, 2, 9, DiskDensity.SD, 32, 0, 1));

        registerFactoryPreset("DSDD (BwG)", DiskFormatFactory.createDsDd());

        // Myarc HFDC
        registerFactoryPreset("HFDC 40-Track DD", DiskFormatFactory.createDsDd());
        registerFactoryPreset("HFDC 80-Track DD", DiskFormatFactory.createHfdc80());

        registerFactoryPreset("HFDC QD",
                DiskFormatFactory.create(80, 2, 36, DiskDensity.HD, 32, 0, 4));

        // Myarc FDC (QD)
        registerFactoryPreset("Myarc FDC QD",
                DiskFormatFactory.create(80, 2, 36, DiskDensity.UD, 32, 0, 4));

        // Horizon RAMdisk (Bonus)
        registerFactoryPreset("Horizon RAMdisk 400K",
                DiskFormatFactory.create(80, 1, 32, DiskDensity.DD, 32, 0, 4));
    }

    // ============================================================
    //  REGISTRATION HELPERS
    // ============================================================

    /** Registers a preset using only a name and a DiskFormat. */
    private static void registerFactoryPreset(String name, DiskFormat format) {
        register(name, DiskFormatFactory.describe(format), format);
    }

    /**
     * Registers a custom preset.
     * Replaces any existing preset with the same name.
     */
    public static void register(String name, String description, DiskFormat format) {
        Objects.requireNonNull(name, "name must not be null");
        PRESETS.put(name.trim(), new Entry(name, description, format));
    }

    // ============================================================
    //  LOOKUP
    // ============================================================

    /** Returns the preset entry with the given name, or empty if not found. */
    public static Optional<Entry> get(String name) {
        Objects.requireNonNull(name, "name must not be null");
        return Optional.ofNullable(PRESETS.get(name.trim()));
    }

    /** Returns the DiskFormat for the given preset name, or empty if not found. */
    public static Optional<DiskFormat> getFormat(String name) {
        return get(name).map(Entry::getFormat);
    }

    // ============================================================
    //  ENUMERATION FOR UI
    // ============================================================

    /** Returns an immutable view of all presets. */
    public static Map<String, Entry> all() {
        return Map.copyOf(PRESETS);
    }

    /** Returns all preset names in registration order. */
    public static String[] names() {
        return PRESETS.keySet().toArray(String[]::new);
    }
}
