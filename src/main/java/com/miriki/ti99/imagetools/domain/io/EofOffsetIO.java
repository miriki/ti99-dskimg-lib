package com.miriki.ti99.imagetools.domain.io;

import java.util.Objects;

import com.miriki.ti99.imagetools.io.Sector;

/**
 * IO helper for reading the EOF offset field from a File Descriptor Record (FDR).
 *
 * The EOF offset is stored at byte 0x10 (decimal 16) in the FDR sector.
 * It represents the number of valid bytes in the last allocated sector.
 */
public final class EofOffsetIO {

    /** Byte offset of the EOF offset field inside the FDR sector */
    private static final int OFFSET = 0x10;

    private EofOffsetIO() {
        // Utility class – no instances
    }

    // ============================================================
    //  PUBLIC API – READ
    // ============================================================

    /**
     * Reads the EOF offset (0–255) from the given FDR sector.
     *
     * @param fdrSector the sector containing the FDR
     * @return EOF offset as unsigned byte
     */
    public static int read(Sector fdrSector) {
        Objects.requireNonNull(fdrSector, "Sector must not be null");

        byte[] raw = fdrSector.getBytes();
        return raw[OFFSET] & 0xFF;
    }
}
