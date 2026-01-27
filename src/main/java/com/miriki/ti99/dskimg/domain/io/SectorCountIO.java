package com.miriki.ti99.dskimg.domain.io;

import java.util.Objects;

import com.miriki.ti99.dskimg.domain.constants.FdrConstants;
import com.miriki.ti99.dskimg.io.Sector;

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

        int hi = raw[FdrConstants.FDR_TOTAL_SECTORS]     & 0xFF;
        int lo = raw[FdrConstants.FDR_TOTAL_SECTORS + 1] & 0xFF;

        return (hi << 8) | lo;
    }
    
    public static void write(Sector fdrSector, int count) {
        Objects.requireNonNull(fdrSector, "Sector must not be null");

        byte[] raw = fdrSector.getBytes();

        raw[FdrConstants.FDR_TOTAL_SECTORS]     = (byte) ((count >> 8) & 0xFF);
        raw[FdrConstants.FDR_TOTAL_SECTORS + 1] = (byte) (count & 0xFF);

        fdrSector.setData(raw);
    }

}
