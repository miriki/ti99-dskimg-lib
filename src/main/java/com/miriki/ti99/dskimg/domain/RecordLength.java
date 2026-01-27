package com.miriki.ti99.dskimg.domain;

import java.util.Objects;

/**
 * Logical record length for TI‑99 files.
 *
 * TI‑99 HFDC rule:
 *   - 80‑byte records  → RL80
 *   - 120‑byte records → RL120
 *   - any other value  → UNKNOWN
 *
 * The logical record length is stored in the FDR at offset 0x11.
 * It is meaningful only for FIXED‑record files.
 */
public enum RecordLength {

    /** 80‑byte fixed records. */
    RL80(80, "80 bytes"),

    /** 120‑byte fixed records. */
    RL120(120, "120 bytes"),

    /** Any unsupported or unknown record length. */
    UNKNOWN(-1, "Unknown");

    /** Raw byte length stored in the FDR. */
    private final int length;

    /** Human‑readable description. */
    private final String description;

    RecordLength(int length, String description) {
        this.length = length;
        this.description = description;
    }

    /** Returns the raw record length in bytes. */
    public int length() {
        return length;
    }

    /** Returns a human‑readable description. */
    public String description() {
        return description;
    }

    /** True if this is a known TI‑99 record length. */
    public boolean isKnown() {
        return this != UNKNOWN;
    }

    /**
     * Determines the record length from an FDR.
     *
     * @param fdr FileDescriptorRecord
     * @return RL80, RL120, or UNKNOWN
     */
    public static RecordLength fromFdr(FileDescriptorRecord fdr) {
        Objects.requireNonNull(fdr, "fdr must not be null");
        return fromLength(fdr.getLogicalRecordLength());
    }

    /**
     * Determines the record length from a raw byte length.
     *
     * @param length raw record length in bytes
     * @return RL80, RL120, or UNKNOWN
     */
    public static RecordLength fromLength(int length) {
        return switch (length) {
            case 80 -> RL80;
            case 120 -> RL120;
            default -> UNKNOWN;
        };
    }

    @Override
    public String toString() {
        return description;
    }
}
