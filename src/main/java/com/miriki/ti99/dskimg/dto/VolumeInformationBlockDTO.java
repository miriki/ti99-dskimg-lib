package com.miriki.ti99.dskimg.dto;

import java.util.Arrays;
import java.util.Objects;

import com.miriki.ti99.dskimg.domain.constants.VibConstants;

/**
 * Immutable DTO representing the 256-byte Volume Information Block (VIB).
 *
 * This DTO transports the raw VIB bytes between layers without exposing
 * mutable internal arrays or interpreting the structure.
 */
public final class VolumeInformationBlockDTO {

    private final byte[] raw;

    // ============================================================
    //  CONSTRUCTOR
    // ============================================================

    public VolumeInformationBlockDTO(byte[] raw) {
        Objects.requireNonNull(raw, "raw must not be null");

        if (raw.length != VibConstants.VIB_SIZE) {
            throw new IllegalArgumentException(
                    "Volume Information Block must be exactly " + VibConstants.VIB_SIZE + " bytes"
            );
        }

        this.raw = Arrays.copyOf(raw, raw.length);
    }

    // ============================================================
    //  ACCESSORS
    // ============================================================

    /** Returns a defensive copy of the raw VIB bytes. */
    public byte[] getRaw() {
        return Arrays.copyOf(raw, raw.length);
    }

    // ============================================================
    //  OBJECT METHODS
    // ============================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VolumeInformationBlockDTO other)) return false;
        return Arrays.equals(raw, other.raw);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(raw);
    }

    @Override
    public String toString() {
        return "VolumeInformationBlockDTO[" +
                "bytes=" + Arrays.toString(Arrays.copyOf(raw, 16)) +
                " ...]";
    }
}
