package com.miriki.ti99.dskimg.domain.io;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import com.miriki.ti99.dskimg.domain.constants.FdrConstants;

/**
 * Utility for encoding and decoding TI-99 file names.
 *
 * TI file names are:
 *   - uppercase ASCII
 *   - max 10 characters
 *   - padded with spaces (0x20)
 *   - stored exactly as 10 bytes in the FDR
 */
public final class FileNameIO {

    /** Fixed TI filename length in bytes */
    // public static final int LENGTH = FdrConstants.FDR_FILE_NAME_SIZE;

    private FileNameIO() {
        // Utility class – no instances
    }

    // ============================================================
    //  PUBLIC API – HIGH-LEVEL NORMALIZATION
    // ============================================================

    /**
     * Converts an arbitrary Java string into a valid TI filename:
     *  - uppercase
     *  - invalid characters replaced with space
     *  - padded/truncated to 10 chars
     */
    public static String toTiFileName(String name) {
        if (name == null) {
            return " ".repeat(FdrConstants.FDR_FILE_NAME_SIZE);
        }

        String upper = name
                .toUpperCase()
                .replaceAll("[^A-Z0-9._]", " "); // later on: regard FIAD?

        // pad right with spaces, then cut to 10 chars
        return String.format("%-" + FdrConstants.FDR_FILE_NAME_SIZE + "s", upper).substring(0, FdrConstants.FDR_FILE_NAME_SIZE);
    }

    // ============================================================
    //  PUBLIC API – ENCODE (String → 10 bytes)
    // ============================================================

    /**
     * Encodes a TI filename into a 10-byte ASCII array.
     * Invalid characters are replaced with spaces.
     */
    public static byte[] encode(String name) {
        byte[] raw = new byte[FdrConstants.FDR_FILE_NAME_SIZE];
        Arrays.fill(raw, (byte) ' ');

        if (name == null) {
            return raw;
        }

        byte[] ascii = toTiFileName(name).getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(ascii, 0, raw, 0, FdrConstants.FDR_FILE_NAME_SIZE);

        return raw;
    }

    // ============================================================
    //  PUBLIC API – DECODE (10 bytes → String)
    // ============================================================

    /**
     * Decodes a 10-byte TI filename into a trimmed Java string.
     */
    public static String decode(byte[] raw) {
        if (raw == null || raw.length == 0) {
            return "";
        }
        return new String(raw, 0, Math.min(raw.length, FdrConstants.FDR_FILE_NAME_SIZE), StandardCharsets.US_ASCII).trim();
    }
}
