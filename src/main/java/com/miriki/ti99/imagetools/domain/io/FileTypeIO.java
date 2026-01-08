package com.miriki.ti99.imagetools.domain.io;

import java.util.Locale;

/**
 * High-level encoder/decoder for TI-99 file types stored in the lower
 * 4 bits of the fileStatus byte (0bFFFF TTTT).
 *
 * Types:
 *   PROGRAM   = 0x01
 *   DIS/FIX   = 0x02
 *   DIS/VAR   = 0x03
 *   INTERNAL  = 0x04
 *
 * Variable/fixed record format is indicated by FLAG_VARIABLE (upper nibble).
 */
public final class FileTypeIO {

    private FileTypeIO() {
        // Utility class – no instances
    }

    // ============================================================
    //  DECODE (status byte → human-readable type)
    // ============================================================

    public static String decode(int status) {

        int type = FileFlagBits.extractType(status);
        boolean variable = FileFlagBits.extractFlags(status) != 0
                && (status & FileFlagBits.FLAG_VARIABLE) != 0;

        return switch (type) {
            case FileFlagBits.TYPE_PROGRAM  -> "PROGRAM";
            case FileFlagBits.TYPE_DISFIX   -> "DIS/FIX";
            case FileFlagBits.TYPE_DISVAR   -> "DIS/VAR";
            case FileFlagBits.TYPE_INTERNAL -> variable ? "INT/VAR" : "INT/FIX";
            default -> "UNKNOWN";
        };
    }

    // ============================================================
    //  ENCODE (human-readable type → status byte)
    // ============================================================

    /**
     * Encodes a TI file type into the lower nibble of the status byte.
     * All other flags (protect, backup, emulate, etc.) remain unchanged.
     */
    public static int encode(String type, int existingFlags) {

        // Remove old type bits (lower nibble)
        int flags = existingFlags & ~FileFlagBits.TYPE_MASK;

        // Remove variable flag (we reapply it if needed)
        flags &= ~FileFlagBits.FLAG_VARIABLE;

        switch (type.toUpperCase(Locale.ROOT)) {

            case "PROGRAM" -> {
                flags |= FileFlagBits.TYPE_PROGRAM;
            }

            case "DIS/FIX" -> {
                flags |= FileFlagBits.TYPE_DISFIX;
            }

            case "DIS/VAR" -> {
                flags |= FileFlagBits.TYPE_DISVAR;
                flags |= FileFlagBits.FLAG_VARIABLE;
            }

            case "INT/FIX" -> {
                flags |= FileFlagBits.TYPE_INTERNAL;
            }

            case "INT/VAR" -> {
                flags |= FileFlagBits.TYPE_INTERNAL;
                flags |= FileFlagBits.FLAG_VARIABLE;
            }

            default -> throw new IllegalArgumentException("Unknown TI-99 file type: " + type);
        }

        return flags;
    }
}
