package com.miriki.ti99.imagetools.domain.io;

import java.util.Objects;

import com.miriki.ti99.imagetools.domain.FileDescriptorRecord;

/**
 * High-level helpers for interpreting the fileStatus byte in a FileDescriptorRecord.
 *
 * The fileStatus byte has the structure:
 *
 *      0bFFFF TTTT
 *
 *  - lower 4 bits (TTTT) = file type
 *  - upper 4 bits (FFFF) = flags
 *
 * This class provides:
 *  - type checks (PROGRAM, DIS/FIX, DIS/VAR, INTERNAL)
 *  - flag checks (PROTECT, INTERNAL, BACKUP, EMULATE, VARIABLE)
 *  - raw access helpers
 */
public final class FileFlagsIO {

    private FileFlagsIO() {
        // Utility class – no instances
    }

    // ============================================================
    //  INTERNAL HELPERS
    // ============================================================

    private static int type(FileDescriptorRecord fdr) {
        Objects.requireNonNull(fdr);
        return FileFlagBits.extractType(fdr.getFlags());
    }

    private static boolean hasFlag(FileDescriptorRecord fdr, int flagMask) {
        Objects.requireNonNull(fdr);
        return (fdr.getFlags() & flagMask) != 0;
    }

    private static boolean notFlag(FileDescriptorRecord fdr, int flagMask) {
        return !hasFlag(fdr, flagMask);
    }

    // ============================================================
    //  FILE TYPE CHECKS (lower nibble)
    // ============================================================

    public static boolean isProgram(FileDescriptorRecord fdr) {
        return type(fdr) == FileFlagBits.TYPE_PROGRAM;
    }

    public static boolean isDisFix(FileDescriptorRecord fdr) {
        return type(fdr) == FileFlagBits.TYPE_DISFIX;
    }

    public static boolean isDisVar(FileDescriptorRecord fdr) {
        return type(fdr) == FileFlagBits.TYPE_DISVAR;
    }

    public static boolean isInternalType(FileDescriptorRecord fdr) {
        return type(fdr) == FileFlagBits.TYPE_INTERNAL;
    }

    // ============================================================
    //  FLAG CHECKS (upper nibble)
    // ============================================================

    public static boolean isProtected(FileDescriptorRecord fdr) {
        return hasFlag(fdr, FileFlagBits.FLAG_PROTECT);
    }

    public static boolean isUnprotected(FileDescriptorRecord fdr) {
        return notFlag(fdr, FileFlagBits.FLAG_PROTECT);
    }

    public static boolean isInternalFlag(FileDescriptorRecord fdr) {
        return hasFlag(fdr, FileFlagBits.FLAG_INTERNAL);
    }

    public static boolean isBackup(FileDescriptorRecord fdr) {
        return hasFlag(fdr, FileFlagBits.FLAG_BACKUP);
    }

    public static boolean isEmulated(FileDescriptorRecord fdr) {
        return hasFlag(fdr, FileFlagBits.FLAG_EMULATE);
    }

    public static boolean isVariableRecord(FileDescriptorRecord fdr) {
        return hasFlag(fdr, FileFlagBits.FLAG_VARIABLE);
    }

    public static boolean isFixedRecord(FileDescriptorRecord fdr) {
        return notFlag(fdr, FileFlagBits.FLAG_VARIABLE);
    }

    // ============================================================
    //  RAW ACCESS
    // ============================================================

    public static int getRawFlags(FileDescriptorRecord fdr) {
        Objects.requireNonNull(fdr);
        return fdr.getFlags();
    }

    public static FileDescriptorRecord withRawFlags(FileDescriptorRecord fdr, int flags) {
        Objects.requireNonNull(fdr);
        return fdr.withFlags(flags);
    }
}
