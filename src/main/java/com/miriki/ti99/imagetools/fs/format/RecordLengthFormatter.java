package com.miriki.ti99.imagetools.fs.format;

import com.miriki.ti99.imagetools.domain.FileDescriptorRecord;
import com.miriki.ti99.imagetools.domain.RecordLength;

/**
 * Formatter for human-readable record length descriptions.
 *
 * Converts the logical record length encoded in the FDR into a
 * descriptive string such as:
 *   - "80 bytes"
 *   - "120 bytes"
 *   - "Unknown"
 *
 * This class does not interpret file formats; it only formats metadata.
 */
public final class RecordLengthFormatter {

    private RecordLengthFormatter() {
        // Utility class – no instances
    }

    // ============================================================
    //  PUBLIC API – FORMAT RECORD LENGTH
    // ============================================================

    /**
     * Returns a human-readable description of the record length.
     *
     * @param fdr the File Descriptor Record
     * @return formatted record length string
     */
    public static String describe(FileDescriptorRecord fdr) {
        RecordLength length = RecordLength.fromFdr(fdr);

        return switch (length) {
            case RL80  -> "80 bytes";
            case RL120 -> "120 bytes";
            default    -> "Unknown";
        };
    }
}
