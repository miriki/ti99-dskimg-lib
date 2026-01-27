package com.miriki.ti99.dskimg.domain;

import java.util.Arrays;
import java.util.Objects;

/**
 * Immutable container for raw file data inside a TI‑99 disk image.
 *
 * Represents:
 *   - the logical TI filename (validated upstream)
 *   - the raw file content as a byte array
 *
 * Used by FileWriter, FileReader, and higher-level services.
 */
public final class FileData {

    /** Logical TI filename. */
    private final String fileName;

    /** Raw file content (immutable via defensive copies). */
    private final byte[] data;

    // ============================================================
    //  CONSTRUCTOR
    // ============================================================

    public FileData(String fileName, byte[] data) {
        this.fileName = Objects.requireNonNull(fileName, "fileName must not be null").trim();
        Objects.requireNonNull(data, "data must not be null");
        this.data = Arrays.copyOf(data, data.length); // defensive copy
    }

    // ============================================================
    //  ACCESSORS
    // ============================================================

    /** Returns the logical TI filename. */
    public String getFileName() {
        return fileName;
    }

    /** Returns a defensive copy of the raw file data. */
    public byte[] getData() {
        return Arrays.copyOf(data, data.length);
    }

    /** Returns the size of the file in bytes. */
    public int size() {
        return data.length;
    }

    // ============================================================
    //  DEBUG STRING
    // ============================================================

    @Override
    public String toString() {
        return "FileData{" +
                "fileName='" + fileName + '\'' +
                ", size=" + data.length +
                '}';
    }
}
