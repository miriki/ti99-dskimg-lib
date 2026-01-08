package com.miriki.ti99.imagetools.domain.io;

import java.util.Objects;

import com.miriki.ti99.imagetools.domain.FileDescriptorRecord;

/**
 * IO helper for reading timestamp fields from a File Descriptor Record (FDR).
 *
 * The HFDC stores timestamps as 16-bit unsigned values:
 *   - creation timestamp
 *   - update timestamp
 *
 * This class provides:
 *   - readCreated()
 *   - readUpdated()
 *   - decode() → best available timestamp
 */
public final class TimestampIO {

    private TimestampIO() {
        // Utility class – no instances
    }

    // ============================================================
    //  PUBLIC API – READ
    // ============================================================

    /**
     * Reads the 16-bit creation timestamp from the FDR.
     */
    public static long readCreated(FileDescriptorRecord fdr) {
        Objects.requireNonNull(fdr, "FileDescriptorRecord must not be null");
        return fdr.getCreationTimestamp() & 0xFFFFL;
    }

    /**
     * Reads the 16-bit update timestamp from the FDR.
     */
    public static long readUpdated(FileDescriptorRecord fdr) {
        Objects.requireNonNull(fdr, "FileDescriptorRecord must not be null");
        return fdr.getUpdateTimestamp() & 0xFFFFL;
    }

    // ============================================================
    //  PUBLIC API – BEST TIMESTAMP
    // ============================================================

    /**
     * Returns the "best" timestamp:
     *   - update timestamp if present
     *   - otherwise creation timestamp
     *   - otherwise 0
     */
    public static long decode(FileDescriptorRecord fdr) {
        if (fdr == null) {
            return 0;
        }

        long updated = readUpdated(fdr);
        if (updated != 0) {
            return updated;
        }

        return readCreated(fdr);
    }
}
