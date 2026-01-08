package com.miriki.ti99.imagetools.domain.io;

import java.util.Objects;

import com.miriki.ti99.imagetools.io.Sector;

/**
 * IO helper for reading the Level-3 record count from a File Descriptor Record (FDR).
 *
 * The Level-3 record count is stored at bytes 18–19 (little endian):
 *
 *   offset 18: low byte
 *   offset 19: high byte
 *
 * This value represents the number of logical Level-3 records in the file.
 */
public final class Level3RecordsIO {

    /** Byte offset of the Level-3 record count inside the FDR */
    private static final int OFFSET = 18;

    private Level3RecordsIO() {
        // Utility class – no instances
    }

    // ============================================================
    //  PUBLIC API – READ
    // ============================================================

    /**
     * Reads the Level-3 record count (little endian) from the given FDR sector.
     *
     * @param fdrSector the sector containing the FDR
     * @return the Level-3 record count (0–65535)
     */
    public static int read(Sector fdrSector) {
        Objects.requireNonNull(fdrSector, "Sector must not be null");

        byte[] raw = fdrSector.getBytes();

        int lo = raw[OFFSET]     & 0xFF;
        int hi = raw[OFFSET + 1] & 0xFF;

        return (hi << 8) | lo;
    }
}
