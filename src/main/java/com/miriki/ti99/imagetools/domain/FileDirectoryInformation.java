package com.miriki.ti99.imagetools.domain;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the HFDC-compatible File Directory Information (FDI) sector.
 *
 * The FDI sector contains a list of sector numbers, each pointing to a
 * File Descriptor Record (FDR). It is the logical directory index used
 * by HFDC/Level-3 disk formats.
 *
 * This class is immutable and contains no IO logic.
 */
public final class FileDirectoryInformation {

    private static final Logger log = LoggerFactory.getLogger(FileDirectoryInformation.class);

    /** Immutable list of FDR sector numbers. */
    private final List<Integer> fdrSectors;

    // ============================================================
    //  CONSTRUCTOR
    // ============================================================

    /**
     * Creates a FileDirectoryInformation instance.
     *
     * @param fdrSectors list of FDR sector numbers
     */
    public FileDirectoryInformation(List<Integer> fdrSectors) {
        log.debug("FileDirectoryInformation({})", fdrSectors);

        Objects.requireNonNull(fdrSectors, "fdrSectors must not be null");
        this.fdrSectors = List.copyOf(fdrSectors); // immutable defensive copy
    }

    // ============================================================
    //  ACCESSORS
    // ============================================================

    /**
     * Returns an unmodifiable list of FDR sector numbers.
     */
    public List<Integer> getFdrSectors() {
        log.debug("getFdrSectors()");
        return Collections.unmodifiableList(fdrSectors);
    }

    /**
     * Returns the number of FDR entries.
     */
    public int getCount() {
        log.debug("getCount()");
        return fdrSectors.size();
    }
}
