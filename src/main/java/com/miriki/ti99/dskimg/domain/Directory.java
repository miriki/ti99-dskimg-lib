package com.miriki.ti99.dskimg.domain;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents the logical directory of a TI‑99 disk.
 *
 * Immutable list of DirectoryEntry objects mapping TI filenames
 * to their File Descriptor Record (FDR) sectors.
 */
public final class Directory {

    /** Immutable list of directory entries. */
    private final List<DirectoryEntry> entries;

    // ============================================================
    //  CONSTRUCTOR
    // ============================================================

    public Directory(List<DirectoryEntry> entries) {
        Objects.requireNonNull(entries, "entries must not be null");
        this.entries = List.copyOf(entries); // immutable defensive copy
    }

    // ============================================================
    //  BASIC ACCESSORS
    // ============================================================

    /** Returns all directory entries (immutable). */
    public List<DirectoryEntry> getEntries() {
        return entries;
    }

    /** True if the directory contains no entries. */
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    // ============================================================
    //  LOOKUP OPERATIONS
    // ============================================================

    /**
     * Modern API: returns an Optional containing the matching entry,
     * or empty if not found.
     */
    public Optional<DirectoryEntry> findOptional(String name) {
        Objects.requireNonNull(name, "name must not be null");

        return entries.stream()
                .filter(e -> e.getFileName().equalsIgnoreCase(name))
                .findFirst();
    }

    /**
     * Legacy API: returns the matching entry or null if not found.
     * Delegates to findOptional().
     */
    public DirectoryEntry findByName(String name) {
        return findOptional(name).orElse(null);
    }

    /** Returns true if the directory contains a file with the given name. */
    public boolean contains(String name) {
        return findOptional(name).isPresent();
    }

    // ============================================================
    //  DEBUG STRING
    // ============================================================

    @Override
    public String toString() {
        return "Directory{" +
                "entries=" + entries +
                '}';
    }
}
