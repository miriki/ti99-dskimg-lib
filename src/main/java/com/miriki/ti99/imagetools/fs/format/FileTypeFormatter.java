package com.miriki.ti99.imagetools.fs.format;

import com.miriki.ti99.imagetools.domain.FileDescriptorRecord;
import com.miriki.ti99.imagetools.domain.FileType;
import com.miriki.ti99.imagetools.domain.RecordLength;

public final class FileTypeFormatter {

    private FileTypeFormatter() {
        // Utility class – no instances
    }

    // ============================================================
    //  PUBLIC API – FORMAT FILE TYPE
    // ============================================================

    /**
     * Returns a human-readable description of the file type.
     */
    public static String describe(FileDescriptorRecord fdr) {

        int status = fdr.getFileStatus();
        FileType type = FileType.fromStatus(status);
        RecordLength length = RecordLength.fromFdr(fdr);

        switch (type) {

            case PROGRAM:
                return "PROGRAM";

            case DISFIX:
                return "DIS/FIX " + length.length();

            case DISVAR:
                return "DIS/VAR " + length.length();

            case INTFIX:
                return "INT/FIX " + length.length();

            case INTVAR:
                return "INT/VAR " + length.length();

            default:
                return "UNKNOWN";
        }
    }
}
