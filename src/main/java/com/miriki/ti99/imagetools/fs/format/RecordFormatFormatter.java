package com.miriki.ti99.imagetools.fs.format;

import com.miriki.ti99.imagetools.domain.FileDescriptorRecord;
import com.miriki.ti99.imagetools.domain.RecordFormat;
import com.miriki.ti99.imagetools.domain.RecordLength;

/**
 * Formatter for human-readable record format descriptions.
 *
 * Produces strings such as:
 *   - "FIXED 80"
 *   - "VARIABLE 120"
 *   - "FIXED"
 *   - "VARIABLE"
 *
 * The formatter combines:
 *   - the record format (FIXED / VARIABLE)
 *   - the logical record length (if known)
 */
public final class RecordFormatFormatter {

    private RecordFormatFormatter() {
        // Utility class – no instances
    }

    // ============================================================
    //  PUBLIC API – FORMAT RECORD FORMAT
    // ============================================================

    /**
     * Returns a human-readable description of the record format.
     *
     * @param fdr the File Descriptor Record
     * @return formatted record format string
     */
    public static String describe(FileDescriptorRecord fdr) {

        RecordFormat format = RecordFormat.fromFdr(fdr);
        RecordLength length = RecordLength.fromFdr(fdr);

        // If record length is unknown, return only FIXED / VARIABLE
        if (length == RecordLength.UNKNOWN) {
            return format.description();
        }

        // Otherwise: FIXED/VARIABLE + max record length
        return format.description() + " " + length.length();
    }
}
