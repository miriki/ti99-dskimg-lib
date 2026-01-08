package com.miriki.ti99.imagetools.domain;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the logical directory of a TI-99 disk.
 *
 * A Directory is an immutable list of DirectoryEntry objects,
 * each mapping a TI filename to the sector number of its
 * File Descriptor Record (FDR).
 *
 * The Directory is typically constructed from:
 *   - the FDI (File Descriptor Index) sector
 *   - the corresponding FDR sectors
 *
 * This class provides:
 *   - read-only access to all entries
 *   - lookup by filename (case-insensitive)
 *   - containment checks
 */
public final class Directory {

    private static final Logger log = LoggerFactory.getLogger(Directory.class);

    /** Immutable list of directory entries. */
    private final List<DirectoryEntry> entries;

    // ============================================================
    //  CONSTRUCTOR
    // ============================================================

    /**
     * Creates a logical directory from a list of entries.
     *
     * @param entries list of DirectoryEntry objects
     */
    public Directory(List<DirectoryEntry> entries) {
        log.debug("[constructor] Directory({})", entries);

        Objects.requireNonNull(entries, "entries must not be null");
        this.entries = List.copyOf(entries); // immutable defensive copy
    }

    // ============================================================
    //  BASIC ACCESSORS
    // ============================================================

    /**
     * Returns an unmodifiable view of all directory entries.
     */
    public List<DirectoryEntry> getEntries() {
        log.debug("getEntries()");
        return Collections.unmodifiableList(entries);
    }

    // ============================================================
    //  LOOKUP OPERATIONS
    // ============================================================

    /**
     * Finds a directory entry by TI filename (case-insensitive).
     *
     * @param name TI filename
     * @return matching DirectoryEntry or null if not found
     */
    public DirectoryEntry findByName(String name) {
        log.debug("findByName({})", name);

        Objects.requireNonNull(name, "name must not be null");

        for (DirectoryEntry e : entries) {
            if (e.getFileName().equalsIgnoreCase(name)) {
                return e;
            }
        }
        return null;
    }

    /**
     * Returns true if the directory contains a file with the given name.
     */
    public boolean contains(String name) {
        log.debug("contains({})", name);
        return findByName(name) != null;
    }
}
