package com.miriki.ti99.imagetools.domain;

/**
 * Logical record format of a TI‑99 file.
 *
 * TI‑99 rule:
 *   - FIXED    → logicalRecordLength > 0
 *   - VARIABLE → logicalRecordLength == 0
 *
 * The record format is derived from the FDR and determines how
 * records are stored and interpreted by the filesystem.
 */
public enum RecordFormat {

    /** Fixed-length records (logicalRecordLength > 0). */
    FIXED("Fixed"),

    /** Variable-length records (logicalRecordLength == 0). */
    VARIABLE("Variable");

    /** Human-readable description. */
    private final String description;

    RecordFormat(String description) {
        this.description = description;
    }

    /** Returns a human-readable description. */
    public String description() {
        return description;
    }

    /**
     * Determines the record format from an FDR.
     *
     * @param fdr FileDescriptorRecord
     * @return FIXED or VARIABLE according to TI‑99 rules
     */
    public static RecordFormat fromFdr(FileDescriptorRecord fdr) {
        return (fdr.getLogicalRecordLength() > 0)
                ? FIXED
                : VARIABLE;
    }
}
