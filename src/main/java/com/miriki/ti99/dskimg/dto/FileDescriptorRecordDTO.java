package com.miriki.ti99.dskimg.dto;

import java.util.Arrays;
import java.util.Objects;

import com.miriki.ti99.dskimg.domain.constants.FdrConstants;

/**
 * Immutable DTO representing a 256-byte File Descriptor Record (FDR).
 *
 * This DTO transports the raw FDR bytes between layers without exposing
 * mutable internal arrays or interpreting the structure.
 */
public final class FileDescriptorRecordDTO {

    private final byte[] raw;

    // ============================================================
    //  CONSTRUCTOR
    // ============================================================

    public FileDescriptorRecordDTO(byte[] raw) {
        Objects.requireNonNull(raw, "raw must not be null");

        if (raw.length != FdrConstants.FDR_SIZE) {
            throw new IllegalArgumentException(
                    "File descriptor record must be exactly " + FdrConstants.FDR_SIZE + " bytes"
            );
        }

        this.raw = Arrays.copyOf(raw, raw.length);
    }

    // ============================================================
    //  ACCESSORS
    // ============================================================

    /** Returns a defensive copy of the raw FDR bytes. */
    public byte[] getRaw() {
        return Arrays.copyOf(raw, raw.length);
    }

    // ============================================================
    //  OBJECT METHODS
    // ============================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileDescriptorRecordDTO other)) return false;
        return Arrays.equals(raw, other.raw);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(raw);
    }

    @Override
    public String toString() {
        return "FileDescriptorRecordDTO[" +
                "bytes=" + Arrays.toString(Arrays.copyOf(raw, 16)) +
                " ...]";
    }
}
