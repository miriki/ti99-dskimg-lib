package com.miriki.ti99.imagetools.domain;

/**
 * TI‑99 HFDC / Level‑3 file types as stored in the FDR status byte.
 *
 * These values occupy the lower 4 bits of the fileStatus byte:
 *
 *   fileStatus = [ FLAGS | TYPE ]
 *
 * Meaning:
 *   - PROGRAM     → executable program file
 *   - DIS/FIX     → fixed‑record display file
 *   - DIS/VAR     → variable‑record display file
 *   - INTERNAL    → internal fixed/variable record file
 *   - DISPLAY     → display file (legacy)
 *   - SEQUENTIAL  → sequential file
 *
 * Any unknown or unsupported type maps to RESERVED.
 */
public enum FileType {

    PROGRAM(0, "Program"),
    DISFIX(1, "DIS/FIX"),
    DISVAR(2, "DIS/VAR"),
    INTERNAL(3, "Internal"),
    DISPLAY(4, "Display"),
    SEQUENTIAL(5, "Sequential"),
    RESERVED(-1, "Reserved");

    /** Numeric code stored in the FDR. */
    private final int code;

    /** Human‑readable description. */
    private final String description;

    FileType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /** Returns the numeric FDR code for this file type. */
    public int code() {
        return code;
    }

    /** Returns a human‑readable description. */
    public String description() {
        return description;
    }

    /**
     * Maps a raw FDR type code to a FileType enum.
     * Unknown values map to RESERVED.
     */
    public static FileType fromCode(int code) {
        return switch (code) {
            case 0 -> PROGRAM;
            case 1 -> DISFIX;
            case 2 -> DISVAR;
            case 3 -> INTERNAL;
            case 4 -> DISPLAY;
            case 5 -> SEQUENTIAL;
            default -> RESERVED;
        };
    }
}
