package com.miriki.ti99.imagetools.domain;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Central registry of known TI-99 disk format presets.
 *
 * This catalog is populated automatically from DiskFormatFactory presets
 * and may be extended at runtime with user-defined formats.
 *
 * Responsibilities:
 *   - lookup by name
 *   - enumeration for UI components
 *   - registration of custom formats
 *
 * The catalog stores immutable {@link Entry} objects and preserves
 * insertion order for predictable UI listing.
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
            this.name = Objects.requireNonNull(name, "name must not be null");
            this.description = Objects.requireNonNull(description, "description must not be null");
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

    /**
     * Registry of presets by name.
     * LinkedHashMap preserves insertion order.
     */
    private static final Map<String, Entry> PRESETS = new LinkedHashMap<>();

    // ============================================================
    //  STATIC INITIALIZATION – BUILT-IN PRESETS
    // ============================================================

    static {
        // TI FDC (SSSD)
        registerFactoryPreset("SSSD (TI FDC)",
                DiskFormatFactory.createSssd());

        // CorComp
        registerFactoryPreset("DSSD (CorComp)",
                DiskFormatFactory.create(40, 2, 9, DiskFormat.Density.SINGLE, 32, 0, 1));
        
        registerFactoryPreset("DSDD (CorComp)",
                DiskFormatFactory.createDsDd());

        // TI DDCC (Double Density)
        registerFactoryPreset("DSDD (TI DDCC)",
                DiskFormatFactory.createDsDd());

        // BwG Controller
        registerFactoryPreset("DSSD (BwG)",
                DiskFormatFactory.create(40, 2, 9, DiskFormat.Density.SINGLE, 32, 0, 1));
        
        registerFactoryPreset("DSDD (BwG)",
                DiskFormatFactory.createDsDd());

        // Myarc HFDC
        registerFactoryPreset("HFDC 40-Track DD",
                DiskFormatFactory.createDsDd());
        
        registerFactoryPreset("HFDC 80-Track DD",
                DiskFormatFactory.createHfdc80());
        
        registerFactoryPreset("HFDC QD",
                DiskFormatFactory.create(80, 2, 36, DiskFormat.Density.HIGH, 32, 0, 4));

        // Myarc FDC (QD)
        registerFactoryPreset("Myarc FDC QD",
                DiskFormatFactory.create(80, 2, 36, DiskFormat.Density.ULTRA, 32, 0, 4));

        // Horizon RAMdisk (Bonus)
        registerFactoryPreset("Horizon RAMdisk 400K",
                DiskFormatFactory.create(80, 1, 32, DiskFormat.Density.DOUBLE, 32, 0, 4));
    }

    private DiskFormatPresetCatalog() {
        // Utility class – no instances
    }

    // ============================================================
    //  REGISTRATION HELPERS
    // ============================================================

    /**
     * Registers a preset using only a name and a DiskFormat.
     * The description is generated automatically.
     */
    private static void registerFactoryPreset(String name, DiskFormat format) {
        String description = DiskFormatFactory.describe(format);
        register(name, description, format);
    }

    /**
     * Registers a custom preset.
     * If a preset with the same name exists, it is replaced.
     */
    public static void register(String name, String description, DiskFormat format) {
        PRESETS.put(name, new Entry(name, description, format));
    }

    // ============================================================
    //  LOOKUP
    // ============================================================

    /**
     * Returns the preset entry with the given name, or null if not found.
     */
    public static Entry get(String name) {
        return PRESETS.get(name);
    }

    /**
     * Returns the DiskFormat for the given preset name, or null if not found.
     */
    public static DiskFormat getFormat(String name) {
        Entry e = PRESETS.get(name);
        return (e != null) ? e.getFormat() : null;
    }

    // ============================================================
    //  ENUMERATION FOR UI
    // ============================================================

    /**
     * Returns an immutable view of all presets.
     */
    public static Map<String, Entry> all() {
        return Collections.unmodifiableMap(PRESETS);
    }

    /**
     * Returns all preset names in registration order.
     */
    public static String[] names() {
        return PRESETS.keySet().toArray(new String[0]);
    }
}
