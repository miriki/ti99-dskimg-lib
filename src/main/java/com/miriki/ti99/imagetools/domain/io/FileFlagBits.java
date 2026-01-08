package com.miriki.ti99.imagetools.domain.io;

/**
 * Bit definitions for the HFDC/TI FDR fileStatus byte.
 *
 * Layout (1 byte):
 *
 *   fileStatus = 0bFFFF TTTT
 *
 *   - lower 4 bits (TTTT) = file type
 *   - upper 4 bits (FFFF) = flags
 *
 * This class provides:
 *   - constants for all known file types
 *   - constants for all known flags
 *   - masks for extraction
 *   - helper methods for combining/extracting values
 */
public final class FileFlagBits {

    private FileFlagBits() {
        // Utility class – no instances
    }

    // ============================================================
    //  FILE TYPES (lower nibble)
    // ============================================================

    /** PROGRAM file (byte stream) */
    public static final int TYPE_PROGRAM  = 0x01;

    /** DIS/FIX file (fixed-length records) */
    public static final int TYPE_DISFIX   = 0x02;

    /** DIS/VAR file (variable-length records) */
    public static final int TYPE_DISVAR   = 0x03;

    /** INTERNAL file (TI OS internal use) */
    public static final int TYPE_INTERNAL = 0x04;

    // Optional / historical:
    // public static final int TYPE_DISPLAY = 0x05;
    // public static final int TYPE_BACKUP  = 0x06;


    // ============================================================
    //  FLAGS (upper nibble)
    //  NOTE: Flags occupy bits 4–7, so they must be shifted!
    // ============================================================

    /** File is protected (read-only) */
    public static final int FLAG_PROTECT  = 0x10; // bit 4

    /** File is internal (system file) */
    public static final int FLAG_INTERNAL = 0x20; // bit 5

    /** File is a backup copy */
    public static final int FLAG_BACKUP   = 0x40; // bit 6

    /** Emulator-specific flag (HFDC extensions) */
    public static final int FLAG_EMULATE  = 0x80; // bit 7

    /** Variable record format (for DIS/VAR) */
    public static final int FLAG_VARIABLE = 0x10; // optional alias


    // ============================================================
    //  MASKS
    // ============================================================

    /** Mask for the lower 4 bits (file type) */
    public static final int TYPE_MASK = 0x0F;

    /** Mask for the upper 4 bits (flags) */
    public static final int FLAG_MASK = 0xF0;


    // ============================================================
    //  HELPER METHODS
    // ============================================================

    /** Extracts the file type (lower nibble). */
    public static int extractType(int fileStatus) {
        return fileStatus & TYPE_MASK;
    }

    /** Extracts the flags (upper nibble). */
    public static int extractFlags(int fileStatus) {
        return fileStatus & FLAG_MASK;
    }

    /** Combines type + flags into a single fileStatus byte. */
    public static int combine(int type, int flags) {
        return (type & TYPE_MASK) | (flags & FLAG_MASK);
    }
}
