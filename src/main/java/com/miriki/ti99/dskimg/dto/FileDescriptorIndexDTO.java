package com.miriki.ti99.dskimg.dto;

import java.util.Arrays;
import java.util.Objects;

import com.miriki.ti99.dskimg.domain.constants.FdiConstants;

/**
 * Immutable DTO representing the 256-byte File Descriptor Index (FDI).
 *
 * This DTO carries the raw FDI bytes without interpreting them.
 */
public final class FileDescriptorIndexDTO {

    private final byte[] entries;

    // ============================================================
    //  CONSTRUCTOR
    // ============================================================

    public FileDescriptorIndexDTO(byte[] entries) {
        Objects.requireNonNull(entries, "entries must not be null");

        if (entries.length != FdiConstants.FDI_SIZE) {
            throw new IllegalArgumentException(
                    "FDI must be exactly " + FdiConstants.FDI_SIZE + " bytes"
            );
        }

        this.entries = Arrays.copyOf(entries, entries.length);
    }

    // ============================================================
    //  ACCESSORS
    // ============================================================

    /** Returns a defensive copy of the raw FDI bytes. */
    public byte[] getEntries() {
        return Arrays.copyOf(entries, entries.length);
    }

    // ============================================================
    //  OBJECT METHODS
    // ============================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileDescriptorIndexDTO other)) return false;
        return Arrays.equals(entries, other.entries);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(entries);
    }

    @Override
    public String toString() {
        return "FileDescriptorIndexDTO[" +
                "bytes=" + Arrays.toString(Arrays.copyOf(entries, 16)) +
                " ...]";
    }
}
