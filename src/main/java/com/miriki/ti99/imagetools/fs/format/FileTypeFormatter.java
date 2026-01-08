package com.miriki.ti99.imagetools.fs.format;

import com.miriki.ti99.imagetools.domain.FileDescriptorRecord;
import com.miriki.ti99.imagetools.domain.FileType;
import com.miriki.ti99.imagetools.domain.RecordFormat;
import com.miriki.ti99.imagetools.domain.RecordLength;

/**
 * Formatter for human-readable TI-99 file type descriptions.
 *
 * Produces strings such as:
 *   - "PROGRAM"
 *   - "DISPLAY"
 *   - "INT/FIX 80"
 *   - "SEQ/VAR 120"
 *   - "DIS/FIX 80"
 *   - "DIS/VAR 120"
 *
 * The formatter combines:
 *   - the file type (PROGRAM, DISPLAY, INTERNAL, SEQUENTIAL, DIS/FIX, DIS/VAR)
 *   - the record format (FIXED / VARIABLE)
 *   - the record length (80 / 120), if applicable
 */
public final class FileTypeFormatter {

    private FileTypeFormatter() {
        // Utility class – no instances
    }

    // ============================================================
    //  PUBLIC API – FORMAT FILE TYPE
    // ============================================================

    /**
     * Returns a human-readable description of the file type.
     *
     * @param fdr the File Descriptor Record
     * @return formatted file type string
     */
    public static String describe(FileDescriptorRecord fdr) {

        FileType type = FileType.fromCode(fdr.getFileType());
        RecordFormat format = RecordFormat.fromFdr(fdr);
        RecordLength length = RecordLength.fromFdr(fdr);

        // PROGRAM, DISPLAY, INTERNAL, SEQUENTIAL have different formatting rules
        switch (type) {

            case PROGRAM:
                return "PROGRAM";

            case DISPLAY:
                return "DISPLAY";

            case INTERNAL:
                return format == RecordFormat.FIXED
                        ? "INT/FIX " + length.length()
                        : "INT/VAR " + length.length();

            case SEQUENTIAL:
                return format == RecordFormat.FIXED
                        ? "SEQ/FIX " + length.length()
                        : "SEQ/VAR " + length.length();

            case DISFIX:
                return "DIS/FIX " + length.length();

            case DISVAR:
                return "DIS/VAR " + length.length();

            default:
                return "UNKNOWN";
        }
    }
}
