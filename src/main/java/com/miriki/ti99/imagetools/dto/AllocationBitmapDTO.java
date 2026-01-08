package com.miriki.ti99.imagetools.dto;

import java.util.Arrays;
import java.util.Objects;

/**
 * Immutable DTO representing a TI-99 Allocation Bitmap (ABM).
 *
 * The ABM is always exactly 200 bytes long and represents the allocation
 * state of up to 1600 sectors (200 * 8 bits).
 *
 * This DTO is used for transferring ABM data between layers without exposing
 * internal mutable arrays.
 */
public final class AllocationBitmapDTO {

    /** Fixed size of the TI allocation bitmap in bytes */
    public static final int SIZE = 200;

    private final byte[] bits;           // 200 bytes ABM
    private final int firstSectorIndex;  // usually 0

    // ============================================================
    //  CONSTRUCTOR
    // ============================================================

    public AllocationBitmapDTO(byte[] bits, int firstSectorIndex) {
        Objects.requireNonNull(bits, "bits must not be null");

        if (bits.length != SIZE) {
            throw new IllegalArgumentException(
                    "Allocation bitmap must be exactly " + SIZE + " bytes"
            );
        }

        this.bits = Arrays.copyOf(bits, bits.length);
        this.firstSectorIndex = firstSectorIndex;
    }

    // ============================================================
    //  ACCESSORS
    // ============================================================

    /**
     * Returns a defensive copy of the 200-byte bitmap.
     */
    public byte[] getBits() {
        return Arrays.copyOf(bits, bits.length);
    }

    /**
     * Returns the first sector index represented by this bitmap.
     * Typically 0, but may differ for exotic formats.
     */
    public int getFirstSectorIndex() {
        return firstSectorIndex;
    }
}
