package com.miriki.ti99.imagetools.dto;

import java.util.Arrays;
import java.util.Objects;

/**
 * Immutable DTO representing a 256-byte File Descriptor Record (FDR).
 *
 * The FDR contains:
 *   - file status and type
 *   - record length
 *   - sector count
 *   - timestamps
 *   - data chain pointers
 *   - file name
 *   - and various HFDC/TI metadata
 *
 * This DTO transports the raw 256-byte FDR between layers without exposing
 * mutable internal arrays.
 */
public final class FileDescriptorRecordDTO {

    /** Fixed size of a TI-99 File Descriptor Record in bytes */
    public static final int SIZE = 256;

    private final byte[] raw;

    // ============================================================
    //  CONSTRUCTOR
    // ============================================================

    public FileDescriptorRecordDTO(byte[] raw) {
        Objects.requireNonNull(raw, "raw must not be null");

        if (raw.length != SIZE) {
            throw new IllegalArgumentException(
                    "File descriptor record must be exactly " + SIZE + " bytes"
            );
        }

        this.raw = Arrays.copyOf(raw, raw.length);
    }

    // ============================================================
    //  ACCESSORS
    // ============================================================

    /**
     * Returns a defensive copy of the 256-byte FDR.
     */
    public byte[] getRaw() {
        return Arrays.copyOf(raw, raw.length);
    }
}
