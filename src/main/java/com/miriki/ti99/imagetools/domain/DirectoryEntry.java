package com.miriki.ti99.imagetools.domain;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a single entry in the logical TI-99 directory.
 *
 * A DirectoryEntry maps:
 *   - a TI filename (max. 10 characters, logical form)
 *   - to the sector number of its File Descriptor Record (FDR)
 *
 * This is a simple immutable value object used throughout the
 * filesystem layer (Directory, DirectoryReader, FileWriter, etc.).
 */
public final class DirectoryEntry {

    private static final Logger log = LoggerFactory.getLogger(DirectoryEntry.class);

    /** Logical TI filename (max. 10 chars, already validated upstream). */
    private final String fileName;

    /** Sector number of the File Descriptor Record (FDR). */
    private final int fdrSector;

    // ============================================================
    //  CONSTRUCTOR
    // ============================================================

    /**
     * Creates a directory entry.
     *
     * @param fileName  TI filename (non-null)
     * @param fdrSector sector number of the FDR
     */
    public DirectoryEntry(String fileName, int fdrSector) {
        log.debug("[constructor] DirectoryEntry({}, {})", fileName, fdrSector);

        this.fileName = Objects.requireNonNull(fileName, "fileName must not be null");
        this.fdrSector = fdrSector;
    }

    // ============================================================
    //  ACCESSORS
    // ============================================================

    /**
     * Returns the TI filename.
     */
    public String getFileName() {
        log.debug("getFileName()");
        return fileName;
    }

    /**
     * Returns the sector number of the File Descriptor Record.
     */
    public int getFdrSector() {
        log.debug("getFdrSector()");
        return fdrSector;
    }
}
