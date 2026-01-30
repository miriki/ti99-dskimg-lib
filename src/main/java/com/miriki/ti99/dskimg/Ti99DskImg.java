package com.miriki.ti99.dskimg;

import java.nio.file.Path;

import com.miriki.ti99.dskimg.domain.DiskFormatPreset;
import com.miriki.ti99.dskimg.impl.Ti99DiskImageImpl;

/**
 * High-level facade providing factory methods and predefined
 * disk format presets for creating and opening TI-99/4A disk images.
 *
 * This class is the public entry point for applications.
 */
public final class Ti99DskImg {

    private Ti99DskImg() {}

    // ============================================================
    // =============== DISK FORMAT PRESETS (PUBLIC) ===============
    // ============================================================

    public static final DiskFormatPreset SSSD40 = DiskFormatPreset.TI_SSSD;
    public static final DiskFormatPreset DSSD40 = DiskFormatPreset.TI_DSSD;
    public static final DiskFormatPreset DSDD40 = DiskFormatPreset.TI_DSDD;

    public static final DiskFormatPreset DSSD80 = DiskFormatPreset.TI_DSSD_80;
    public static final DiskFormatPreset DSDD80 = DiskFormatPreset.TI_DSDD_80;

    // ============================================================
    // ======================== FACTORY API ========================
    // ============================================================

    /**
     * Opens an existing disk image from disk.
     */
    public static Ti99DiskImage open(Path path) {
        return new Ti99DiskImageImpl(path);
    }

    /**
     * Creates a new, empty disk image using the given preset.
     */
    public static Ti99DiskImage createEmptyImage(DiskFormatPreset preset) {
        return new Ti99DiskImageImpl(preset);
    }

    /**
     * Creates a new, empty disk image and immediately saves it.
     */
    public static Ti99DiskImage createEmptyImage(Path target, DiskFormatPreset preset) {
        Ti99DiskImage img = new Ti99DiskImageImpl(preset);
        img.save(target);
        return img;
    }
}
