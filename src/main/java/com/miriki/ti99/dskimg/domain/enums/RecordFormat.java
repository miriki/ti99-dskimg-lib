package com.miriki.ti99.dskimg.domain.enums;

import com.miriki.ti99.dskimg.domain.enums.RecordFormat;

public enum RecordFormat {
	// Bit 7
    FIX(0),
    VAR(1);

    private final int code;

    RecordFormat(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static RecordFormat fromCode(int code) {
        for (RecordFormat f : values()) {
            if (f.code == code) return f;
        }
        throw new IllegalArgumentException("Unknown record format code: " + code);
    }
}
