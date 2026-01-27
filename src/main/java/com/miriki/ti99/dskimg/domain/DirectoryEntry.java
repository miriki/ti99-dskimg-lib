package com.miriki.ti99.dskimg.domain;

import java.util.Objects;

/**
 * Represents a single entry in the logical TI‑99 directory.
 *
 * Immutable value object mapping:
 *   - a TI filename (logical form, max. 10 chars)
 *   - to the sector number of its File Descriptor Record (FDR)
 */
public final class DirectoryEntry {

    /** Logical TI filename (max. 10 chars, validated upstream). */
    private final String fileName;

    /** Sector number of the File Descriptor Record (FDR). */
    private final int fdrSector;

    // ============================================================
    //  CONSTRUCTOR
    // ============================================================

    public DirectoryEntry(String fileName, int fdrSector) {

        this.fileName = Objects.requireNonNull(fileName, "fileName must not be null").trim();

        if (fdrSector < 0) {
            throw new IllegalArgumentException("FDR sector must be >= 0");
        }
        this.fdrSector = fdrSector;
    }

    // ============================================================
    //  ACCESSORS
    // ============================================================

    public String getFileName() {
        return fileName;
    }

    public int getFdrSector() {
        return fdrSector;
    }

    // ============================================================
    //  DEBUG STRING
    // ============================================================

    @Override
    public String toString() {
        return "DirectoryEntry{" +
                "fileName='" + fileName + '\'' +
                ", fdrSector=" + fdrSector +
                '}';
    }
}
