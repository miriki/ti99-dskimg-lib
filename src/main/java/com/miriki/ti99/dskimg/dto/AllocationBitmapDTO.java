package com.miriki.ti99.dskimg.dto;

import java.util.Arrays;
import java.util.Objects;

import com.miriki.ti99.dskimg.domain.constants.VibConstants;

/**
 * Immutable DTO representing a TI-99 Allocation Bitmap (ABM).
 *
 * The ABM is always exactly 200 bytes long and represents the allocation
 * state of up to 1600 logical sectors (200 * 8 bits).
 *
 * This DTO is used for transferring ABM data between layers without exposing
 * internal mutable arrays.
 */
public final class AllocationBitmapDTO {

    private final byte[] bits;           // 200 bytes ABM
    private final int firstSectorIndex;  // logical sector index (usually 0)

    // ============================================================
    //  CONSTRUCTOR
    // ============================================================

    public AllocationBitmapDTO(byte[] bits, int firstSectorIndex) {
        Objects.requireNonNull(bits, "bits must not be null");

        if (bits.length != VibConstants.VIB_ABM_SIZE) {
            throw new IllegalArgumentException(
                    "Allocation bitmap must be exactly " + VibConstants.VIB_ABM_SIZE + " bytes"
            );
        }

        this.bits = Arrays.copyOf(bits, bits.length);
        this.firstSectorIndex = firstSectorIndex;
    }

    // ============================================================
    //  ACCESSORS
    // ============================================================

    /** Returns a defensive copy of the 200-byte bitmap. */
    public byte[] getBits() {
        return Arrays.copyOf(bits, bits.length);
    }

    /**
     * Returns the first logical sector index represented by this bitmap.
     * Typically 0, but may differ for exotic formats or DSR abstractions.
     */
    public int getFirstSectorIndex() {
        return firstSectorIndex;
    }

    // ============================================================
    //  CONVENIENCE
    // ============================================================

    /** Returns true if the given logical sector is marked as allocated. */
    public boolean isAllocated(int logicalSector) {
        int idx = logicalSector - firstSectorIndex;

        int maxBits = VibConstants.VIB_ABM_SIZE * 8;

        if (idx < 0 || idx >= maxBits) {
            throw new IndexOutOfBoundsException("Logical sector " + logicalSector);
        }

        int byteIndex = idx / 8;
        int bitIndex  = idx % 8;

        return (bits[byteIndex] & (1 << bitIndex)) != 0;
    }

    // ============================================================
    //  OBJECT METHODS
    // ============================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AllocationBitmapDTO other)) return false;
        return firstSectorIndex == other.firstSectorIndex
                && Arrays.equals(bits, other.bits);
    }

    @Override
    public int hashCode() {
        int result = Integer.hashCode(firstSectorIndex);
        result = 31 * result + Arrays.hashCode(bits);
        return result;
    }

    @Override
    public String toString() {
        return "AllocationBitmapDTO[firstSector=" + firstSectorIndex
                + ", bits=" + Arrays.toString(Arrays.copyOf(bits, 8)) + "...]";
    }
}
