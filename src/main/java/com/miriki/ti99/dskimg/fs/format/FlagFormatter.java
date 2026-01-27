package com.miriki.ti99.dskimg.fs.format;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.miriki.ti99.dskimg.domain.FileAttribute;

public final class FlagFormatter {

    private FlagFormatter() {}

    public static String describe(Set<FileAttribute> attrs) {

        List<String> flags = new ArrayList<>(3);

        if (attrs.contains(FileAttribute.PROTECTED)) flags.add("Protected");
        if (attrs.contains(FileAttribute.BACKUP))    flags.add("Backup");
        if (attrs.contains(FileAttribute.EMULATED))  flags.add("Emulated");

        return flags.isEmpty()
                ? "None"
                : String.join(", ", flags);
    }
}
