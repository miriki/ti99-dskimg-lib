package com.miriki.ti99.dskimg.domain;

import java.util.List;
import java.util.Objects;

/**
 * Represents the HFDC-compatible File Directory Information (FDI) sector.
 *
 * The FDI sector contains a list of sector numbers, each pointing to a
 * File Descriptor Record (FDR). It is the logical directory index used
 * by HFDC/Level-3 disk formats.
 *
 * Immutable and contains no IO logic.
 */
public final class FileDirectoryInformation {

    /** Immutable list of FDR sector numbers. */
    private final List<Integer> fdrSectors;

    // ============================================================
    //  CONSTRUCTOR
    // ============================================================

    public FileDirectoryInformation(List<Integer> fdrSectors) {
        Objects.requireNonNull(fdrSectors, "fdrSectors must not be null");
        this.fdrSectors = List.copyOf(fdrSectors); // immutable defensive copy
    }

    // ============================================================
    //  ACCESSORS
    // ============================================================

    /** Returns the immutable list of FDR sector numbers. */
    public List<Integer> getFdrSectors() {
        return fdrSectors;
    }

    /** Returns the number of FDR entries. */
    public int getCount() {
        return fdrSectors.size();
    }

    /** True if the FDI contains no entries. */
    public boolean isEmpty() {
        return fdrSectors.isEmpty();
    }

    // ============================================================
    //  DEBUG STRING
    // ============================================================

    @Override
    public String toString() {
        return "FileDirectoryInformation{" +
                "fdrSectors=" + fdrSectors +
                '}';
    }
}
