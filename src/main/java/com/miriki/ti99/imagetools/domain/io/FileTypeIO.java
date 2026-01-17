package com.miriki.ti99.imagetools.domain.io;

// import java.util.Locale;

import com.miriki.ti99.imagetools.domain.FileType;

/**
 * High-level encoder/decoder for TI-99 file types stored in the lower
 * 4 bits of the FDR fileStatus byte (0bFFFF TTTT).
 *
 * Types (lower nibble):
 *   PROGRAM   = 0x01
 *   DIS/FIX   = 0x02
 *   DIS/VAR   = 0x03
 *   INT/FIX   = 0x04
 *   INT/VAR   = 0x05
 *
 * Only the lower nibble is handled here.
 * Upper-nibble flags (PROTECTED, BACKUP, EMULATE) remain unchanged.
 */
public final class FileTypeIO {

    private FileTypeIO() {
        // Utility class – no instances
    }

    // ============================================================
    //  DECODE (status byte → human-readable type)
    // ============================================================

    public static String decode(int status) {

        FileType type = FileType.fromStatus(status);

        return switch (type) {
            case PROGRAM -> "PROGRAM";
            case DISFIX  -> "DIS/FIX";
            case DISVAR  -> "DIS/VAR";
            case INTFIX  -> "INT/FIX";
            case INTVAR  -> "INT/VAR";
            default      -> "UNKNOWN";
        };
    }

    // ============================================================
    //  ENCODE (human-readable type → status byte)
    // ============================================================

    /**
     * Encodes a TI file type into the lower nibble of the status byte.
     * All other flags (protect, backup, emulate, etc.) remain unchanged.
     */
    /*
    public static int encode(String type, int existingStatus) {

        // Nur PROTECT/BACKUP/EMULATE behalten
        int status = existingStatus & (0x08 | 0x10 | 0x20);

        switch (type.toUpperCase(Locale.ROOT)) {

            case "PROGRAM" -> {
                status |= 0x01; // Bit 0 = Program
            }

            case "DIS/FIX" -> {
                // Bit 0 = 0 (Data)
                // Bit 1 = 0 (Display)
                // Bit 7 = 0 (Fixed)
            }

            case "DIS/VAR" -> {
                status |= 0x80; // Bit 7 = Variable
            }

            case "INT/FIX" -> {
                status |= 0x02; // Bit 1 = Internal
            }

            case "INT/VAR" -> {
                status |= 0x02; // Internal
                status |= 0x80; // Variable
            }

            default -> throw new IllegalArgumentException("Unknown TI-99 file type: " + type);
        }

        return status;
    }
    */
}
