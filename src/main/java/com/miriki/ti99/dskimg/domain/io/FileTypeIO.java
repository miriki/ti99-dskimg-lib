package com.miriki.ti99.dskimg.domain.io;

import com.miriki.ti99.dskimg.domain.FdrStatus;
import com.miriki.ti99.dskimg.domain.constants.FileTypeStrings;
import com.miriki.ti99.dskimg.domain.enums.FileType;
import com.miriki.ti99.dskimg.domain.enums.RecordFormat;

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
 *
 * NOTE:
 *   This class now delegates decoding to the new FdrStatus abstraction,
 *   which cleanly separates FileType, RecordFormat and FileAttributes.
 */
public final class FileTypeIO {

    private FileTypeIO() {
        // Utility class – no instances
    }

    // ============================================================
    //  DECODE (status byte → human-readable type)
    // ============================================================

    public static FileType decode(int status) {

        FdrStatus s = FdrStatus.fromByte(status);
        return s.getType();
    }

    public static RecordFormat decodeFormat(int status) {

        FdrStatus s = FdrStatus.fromByte(status);
        return s.getFormat();
    }

    public static String toUiLabel(FileType type, RecordFormat format, int recordLength) {

        return switch (type) {
            case PGM -> FileTypeStrings.PROGRAM;

            case DIS -> (format == RecordFormat.FIX)
                        ? FileTypeStrings.DIS_FIX
                        : FileTypeStrings.DIS_VAR;

            case INT -> (format == RecordFormat.FIX)
                        ? FileTypeStrings.INT_FIX
                        : FileTypeStrings.INT_VAR;
        };
    }

    // ============================================================
    //  ENCODE (human-readable type → status byte)
    // ============================================================

    /**
     * Encodes a TI file type into the lower nibble of the status byte.
     * All other flags (protect, backup, emulate, etc.) remain unchanged.
     *
     * NOTE:
     *   This method now uses FdrStatus to construct the correct status byte.
     */
    public static int encode(String type, int existingStatus) {

        FdrStatus old = FdrStatus.fromByte(existingStatus);

        return switch (type.toUpperCase()) {

            case FileTypeStrings.PROGRAM ->
                new FdrStatus(FileType.PGM, RecordFormat.FIX, old.getAttributes()).toByte();

            case FileTypeStrings.DIS_FIX ->
                new FdrStatus(FileType.DIS, RecordFormat.FIX, old.getAttributes()).toByte();

            case FileTypeStrings.DIS_VAR ->
                new FdrStatus(FileType.DIS, RecordFormat.VAR, old.getAttributes()).toByte();

            case FileTypeStrings.INT_FIX ->
                new FdrStatus(FileType.INT, RecordFormat.FIX, old.getAttributes()).toByte();

            case FileTypeStrings.INT_VAR ->
                new FdrStatus(FileType.INT, RecordFormat.VAR, old.getAttributes()).toByte();

            default ->
                throw new IllegalArgumentException("Unknown TI-99 file type: " + type);
        };
    }
}
