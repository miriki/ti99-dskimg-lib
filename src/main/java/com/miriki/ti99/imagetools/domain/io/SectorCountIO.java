package com.miriki.ti99.imagetools.domain.io;

import java.util.Objects;

import com.miriki.ti99.imagetools.io.Sector;

/**
 * IO helper for reading the "used sector count" field from a File Descriptor Record (FDR).
 *
 * The used sector count is stored at bytes 14–15 (big endian):
 *
 *   offset 14: high byte
 *   offset 15: low byte
 *
 * This value represents the number of sectors allocated to the file.
 */
public final class SectorCountIO {

    /** Byte offset of the used sector count inside the FDR */
    private static final int OFFSET = 14;

    private SectorCountIO() {
        // Utility class – no instances
    }

    // ============================================================
    //  PUBLIC API – READ
    // ============================================================

    /**
     * Reads the used sector count (big endian) from the given FDR sector.
     *
     * @param fdrSector the sector containing the FDR
     * @return number of allocated sectors (0–65535)
     */
    public static int read(Sector fdrSector) {
        Objects.requireNonNull(fdrSector, "Sector must not be null");

        byte[] raw = fdrSector.getBytes();

        int hi = raw[OFFSET]     & 0xFF;
        int lo = raw[OFFSET + 1] & 0xFF;

        return (hi << 8) | lo;
    }
}
