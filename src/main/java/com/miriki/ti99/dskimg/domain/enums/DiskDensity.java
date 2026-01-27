package com.miriki.ti99.dskimg.domain.enums;

public enum DiskDensity {
	XX(0),
    SD(1),
    DD(2),
    HD(3),
    UD(4);

    private final int code;

    DiskDensity(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static DiskDensity fromCode(int code) {
        for (DiskDensity d : values()) {
            if (d.code == code) return d;
        }
        throw new IllegalArgumentException("Unknown density code: " + code);
    }
}
