package com.miriki.ti99.imagetools.domain.io;

import java.util.Objects;

import com.miriki.ti99.imagetools.io.Sector;

/**
 * IO helper for reading the logical record length from a File Descriptor Record (FDR).
 *
 * The logical record length is stored at byte offset 17 in the FDR sector.
 * This value applies to DIS/FIX and DIS/VAR files.
 */
public final class RecordLengthIO {

    /** Byte offset of the logical record length inside the FDR */
    private static final int OFFSET = 17;

    private RecordLengthIO() {
        // Utility class – no instances
    }

    // ============================================================
    //  PUBLIC API – READ
    // ============================================================

    /**
     * Reads the logical record length (0–255) from the given FDR sector.
     *
     * @param fdrSector the sector containing the FDR
     * @return logical record length as unsigned byte
     */
    public static int read(Sector fdrSector) {
        Objects.requireNonNull(fdrSector, "Sector must not be null");

        byte[] raw = fdrSector.getBytes();
        return raw[OFFSET] & 0xFF;
    }
}
