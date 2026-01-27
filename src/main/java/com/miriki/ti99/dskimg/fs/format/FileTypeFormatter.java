package com.miriki.ti99.dskimg.fs.format;

import com.miriki.ti99.dskimg.domain.FileDescriptorRecord;
import com.miriki.ti99.dskimg.domain.RecordLength;

public final class FileTypeFormatter {

    private FileTypeFormatter() {}

    public static String describe(FileDescriptorRecord fdr) {
        var status = fdr.getStatus();
        var type = status.getType();
        var format = status.getFormat();
        var length = RecordLength.fromFdr(fdr).length();
        return type.formatLabel(format, length);
    }
}
