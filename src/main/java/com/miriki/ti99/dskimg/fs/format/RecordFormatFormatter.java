package com.miriki.ti99.dskimg.fs.format;

import com.miriki.ti99.dskimg.domain.enums.RecordFormat;

/**
 * Formatter for human-readable record format descriptions.
 *
 * Produces strings such as:
 *   - "Fixed"
 *   - "Variable"
 */
public final class RecordFormatFormatter {

    private RecordFormatFormatter() {}

    /**
     * Returns a human-readable record format ("Fixed" / "Variable").
     */
    public static String describe(RecordFormat format) {
        return (format == RecordFormat.FIX) ? "Fixed" : "Variable";
    }
}
