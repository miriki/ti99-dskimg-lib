package com.miriki.ti99.dskimg.dto;

import java.util.Arrays;
import java.util.Objects;

/**
 * Immutable DTO representing a 3-byte HFDC Data Chain Pointer (DCP).
 *
 * This DTO carries the raw DCP bytes without interpreting structure size.
 */
public final class DataChainPointerDTO {

    private final byte[] raw;

    // ============================================================
    //  CONSTRUCTOR
    // ============================================================

    public DataChainPointerDTO(byte[] raw) {
        Objects.requireNonNull(raw, "raw must not be null");

        // Optional: defensiv, aber ohne eigene SIZE-Konstante
        if (raw.length != 3) {
            throw new IllegalArgumentException("DCP must be exactly 3 bytes");
        }

        this.raw = Arrays.copyOf(raw, raw.length);
    }

    // ============================================================
    //  ACCESSORS
    // ============================================================

    public byte[] getRaw() {
        return Arrays.copyOf(raw, raw.length);
    }

    public int getStart() {
        int b0 = raw[0] & 0xFF;
        int b1 = raw[1] & 0xFF;

        int N1 =  b0        & 0x0F;
        int N2 = (b0 >> 4)  & 0x0F;
        int N3 =  b1        & 0x0F;

        return (N3 << 8) | (N2 << 4) | N1;
    }

    public int getLength() {
        int b1 = raw[1] & 0xFF;
        int b2 = raw[2] & 0xFF;

        int M1 = (b1 >> 4)  & 0x0F;
        int M2 =  b2        & 0x0F;
        int M3 = (b2 >> 4)  & 0x0F;

        return (M3 << 8) | (M2 << 4) | M1;
    }

    // ============================================================
    //  OBJECT METHODS
    // ============================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataChainPointerDTO other)) return false;
        return Arrays.equals(raw, other.raw);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(raw);
    }

    @Override
    public String toString() {
        return String.format(
                "DCP[start=%d, length=%d, raw=%02X %02X %02X]",
                getStart(),
                getLength(),
                raw[0] & 0xFF,
                raw[1] & 0xFF,
                raw[2] & 0xFF
        );
    }
}
