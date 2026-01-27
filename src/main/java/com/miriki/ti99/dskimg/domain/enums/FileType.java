package com.miriki.ti99.dskimg.domain.enums;

public enum FileType {
    PGM(0, "PROGRAM"),
    DIS(1, "DIS"),
    INT(2, "INT");

    private final int code;
    private final String baseLabel;

    FileType(int code, String baseLabel) {
        this.code = code;
        this.baseLabel = baseLabel;
    }

    public int getCode() {
        return code;
    }

    public String getBaseLabel() {
        return baseLabel;
    }

    public String formatLabel(RecordFormat format, int recordLength) {
        return switch (this) {
            case PGM -> baseLabel;
            default -> baseLabel + "/" + (format == RecordFormat.FIX ? "FIX" : "VAR") + " " + recordLength;
        };
    }

    public static FileType fromCode(int code) {
        for (FileType t : values()) {
            if (t.code == code) return t;
        }
        throw new IllegalArgumentException("Unknown file type code: " + code);
    }
}
