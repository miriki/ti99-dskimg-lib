package com.miriki.ti99.dskimg.domain;

/**
 * File attributes stored in the TI‑99 FDR status byte.
 *
 * These attributes occupy bits 3–5 of the status byte:
 *   - PROTECTED (bit 3)
 *   - BACKUP    (bit 4)
 *   - EMULATED  (bit 5)
 *
 * Other bits (0–1 = FileType, 7 = RecordFormat) are handled elsewhere.
 */
public enum FileAttribute {

    /** Bit 3: file is protected (read‑only). */
    PROTECTED (0x08),

    /** Bit 4: file is marked as backup. */
    BACKUP    (0x10),

    /** Bit 5: file is emulated (HFDC/ramdisk semantics). */
    EMULATED  (0x20);

    /** Bit mask for this attribute within the FDR status byte. */
    public final int mask;

    FileAttribute(int mask) {
        this.mask = mask;
    }
}
