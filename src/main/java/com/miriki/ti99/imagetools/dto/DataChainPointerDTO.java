package com.miriki.ti99.imagetools.dto;

import java.util.Arrays;
import java.util.Objects;

/**
 * Immutable DTO representing a 3-byte HFDC Data Chain Pointer (DCP).
 *
 * A DCP entry has the structure:
 *   - byte 0: high byte of start sector
 *   - byte 1: low byte of start sector
 *   - byte 2: (length-1)<<4 (upper nibble), lower nibble unused
 *
 * This DTO is used to transfer raw DCP data between layers without exposing
 * mutable internal arrays.
 */
public final class DataChainPointerDTO {

    /** Fixed size of a Data Chain Pointer in bytes */
    public static final int SIZE = 3;

    private final byte[] raw;

    // ============================================================
    //  CONSTRUCTOR
    // ============================================================

    public DataChainPointerDTO(byte[] raw) {
        Objects.requireNonNull(raw, "raw must not be null");

        if (raw.length != SIZE) {
            throw new IllegalArgumentException(
                    "Data chain pointer must be exactly " + SIZE + " bytes"
            );
        }

        this.raw = Arrays.copyOf(raw, raw.length);
    }

    // ============================================================
    //  ACCESSORS
    // ============================================================

    /**
     * Returns a defensive copy of the 3-byte raw DCP data.
     */
    public byte[] getRaw() {
        return Arrays.copyOf(raw, raw.length);
    }
}
