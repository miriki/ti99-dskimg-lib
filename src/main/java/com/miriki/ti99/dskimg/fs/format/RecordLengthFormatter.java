package com.miriki.ti99.dskimg.fs.format;

import com.miriki.ti99.dskimg.domain.RecordLength;

public final class RecordLengthFormatter {

    private RecordLengthFormatter() {}

    public static String describe(RecordLength length) {
        return length.length() + " bytes";
    }
}
