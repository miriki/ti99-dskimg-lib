package com.miriki.ti99.imagetools.domain;

/**
 * Bit flags stored in the HFDC/Level-3 FDR status byte.
 *
 * These flags occupy the upper 4 bits of the fileStatus byte:
 *
 *   fileStatus = [ FLAGS | TYPE ]
 *
 * where:
 *   - TYPE  = lower 4 bits (0x0F)
 *   - FLAGS = upper 4 bits (0xF0)
 *
 * Each enum constant represents one bit mask.
 */
public enum FileFlagBits {

    /** File is write-protected. */
    PROTECTED(0x08),

    /** File is marked as backup copy. */
    BACKUP(0x10),

    /** File uses "emulate" mode (controller-specific). */
    EMULATE(0x20);

    /** Bit mask for this flag. */
    public final int mask;

    FileFlagBits(int mask) {
        this.mask = mask;
    }
}
