package com.miriki.ti99.imagetools.fs.format;

import java.util.ArrayList;
import java.util.List;

import com.miriki.ti99.imagetools.domain.FileDescriptorRecord;
import com.miriki.ti99.imagetools.domain.FileFlagBits;

/**
 * Formatter for human-readable file flag descriptions.
 *
 * Produces strings such as:
 *   - "Protected"
 *   - "Protected, Backup"
 *   - "None"
 *
 * Flags are extracted from the File Descriptor Record (FDR).
 */
public final class FlagFormatter {

    private FlagFormatter() {
        // Utility class – no instances
    }

    // ============================================================
    //  PUBLIC API – FORMAT FLAGS
    // ============================================================

    /**
     * Returns a human-readable list of all flags set in the FDR.
     *
     * @param fdr the File Descriptor Record
     * @return comma-separated list of flags, or "None"
     */
    public static String describe(FileDescriptorRecord fdr) {

        List<String> flags = new ArrayList<>(3);

        if (fdr.hasFlag(FileFlagBits.PROTECTED)) flags.add("Protected");
        if (fdr.hasFlag(FileFlagBits.BACKUP))    flags.add("Backup");
        if (fdr.hasFlag(FileFlagBits.EMULATE))   flags.add("Emulate");

        return flags.isEmpty()
                ? "None"
                : String.join(", ", flags);
    }
}
