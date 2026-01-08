package com.miriki.ti99.imagetools.domain;

import java.util.Arrays;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Immutable container for raw file data inside a TI‑99 disk image.
 *
 * This class represents:
 *   - the logical TI filename (already validated upstream)
 *   - the raw file content as a byte array
 *
 * It is used by FileWriter, FileReader, and higher-level services
 * to pass file data around without exposing mutable internal state.
 */
public final class FileData {

    private static final Logger log = LoggerFactory.getLogger(FileData.class);

    /** Logical TI filename. */
    private final String fileName;

    /** Raw file content (immutable via defensive copies). */
    private final byte[] data;

    // ============================================================
    //  CONSTRUCTOR
    // ============================================================

    /**
     * Creates a FileData object.
     *
     * @param fileName logical TI filename
     * @param data     raw file content
     */
    public FileData(String fileName, byte[] data) {
        log.debug("[constructor] FileData({}, data[{}])", fileName,
                (data != null ? data.length : -1));

        this.fileName = Objects.requireNonNull(fileName, "fileName must not be null");
        this.data = Objects.requireNonNull(data, "data must not be null");
    }

    // ============================================================
    //  ACCESSORS
    // ============================================================

    /**
     * Returns the logical TI filename.
     */
    public String getFileName() {
        log.debug("getFileName()");
        return fileName;
    }

    /**
     * Returns a defensive copy of the raw file data.
     */
    public byte[] getData() {
        log.debug("getData()");
        return Arrays.copyOf(data, data.length);
    }

    /**
     * Returns the size of the file in bytes.
     */
    public int size() {
        log.debug("size()");
        return data.length;
    }
}
