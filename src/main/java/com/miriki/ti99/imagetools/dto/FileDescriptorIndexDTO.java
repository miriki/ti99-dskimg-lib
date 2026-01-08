package com.miriki.ti99.imagetools.dto;

import java.util.Arrays;
import java.util.Objects;

/**
 * Immutable DTO representing the 256-byte File Descriptor Index (FDI).
 *
 * The FDI is a table of 128 entries (2 bytes each), where each entry
 * contains the sector number of a File Descriptor Record (FDR).
 *
 * This DTO is used to transfer raw FDI data between layers without exposing
 * mutable internal arrays.
 */
public final class FileDescriptorIndexDTO {

    /** Fixed size of the FDI in bytes */
    public static final int SIZE = 256;

    private final byte[] entries;

    // ============================================================
    //  CONSTRUCTOR
    // ============================================================

    public FileDescriptorIndexDTO(byte[] entries) {
        Objects.requireNonNull(entries, "entries must not be null");

        if (entries.length != SIZE) {
            throw new IllegalArgumentException(
                    "File descriptor index must be exactly " + SIZE + " bytes"
            );
        }

        this.entries = Arrays.copyOf(entries, entries.length);
    }

    // ============================================================
    //  ACCESSORS
    // ============================================================

    /**
     * Returns a defensive copy of the 256-byte FDI table.
     */
    public byte[] getEntries() {
        return Arrays.copyOf(entries, entries.length);
    }
}
