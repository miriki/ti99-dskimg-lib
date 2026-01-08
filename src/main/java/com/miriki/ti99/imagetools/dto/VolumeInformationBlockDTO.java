package com.miriki.ti99.imagetools.dto;

import java.util.Arrays;
import java.util.Objects;

/**
 * Immutable DTO representing the 256-byte Volume Information Block (VIB).
 *
 * The VIB contains:
 *   - volume name
 *   - geometry information (tracks, sides, sectors)
 *   - density and format metadata
 *   - allocation bitmap pointer
 *   - timestamps and identifiers
 *
 * This DTO transports the raw 256-byte VIB between layers without exposing
 * mutable internal arrays.
 */
public final class VolumeInformationBlockDTO {

    /** Fixed size of a TI-99 Volume Information Block in bytes */
    public static final int SIZE = 256;

    private final byte[] raw;

    // ============================================================
    //  CONSTRUCTOR
    // ============================================================

    public VolumeInformationBlockDTO(byte[] raw) {
        Objects.requireNonNull(raw, "raw must not be null");

        if (raw.length != SIZE) {
            throw new IllegalArgumentException(
                    "Volume information block must be exactly " + SIZE + " bytes"
            );
        }

        this.raw = Arrays.copyOf(raw, raw.length);
    }

    // ============================================================
    //  ACCESSORS
    // ============================================================

    /**
     * Returns a defensive copy of the 256-byte VIB.
     */
    public byte[] getRaw() {
        return Arrays.copyOf(raw, raw.length);
    }
}
