package com.miriki.ti99.dskimg.domain.enums;

public enum DiskSides {
    SS(1),
    DS(2);

    private final int code;

    DiskSides(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static DiskSides fromCode(int code) {
        for (DiskSides d : values()) {
            if (d.code == code) return d;
        }
        throw new IllegalArgumentException("Unknown sides code: " + code);
    }
}
