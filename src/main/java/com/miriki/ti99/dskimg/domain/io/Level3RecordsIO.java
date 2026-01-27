package com.miriki.ti99.dskimg.domain.io;

import java.util.Objects;

import com.miriki.ti99.dskimg.domain.constants.FdrConstants;
import com.miriki.ti99.dskimg.io.Sector;

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

        int lo = raw[FdrConstants.FDR_LEVEL3_RECORDS_USED] & 0xFF;
        int hi = raw[FdrConstants.FDR_LEVEL3_RECORDS_USED + 1] & 0xFF;

        return (hi << 8) | lo;
    }
    
    public static void write(Sector fdrSector, int count) {
        Objects.requireNonNull(fdrSector, "Sector must not be null");

        byte[] raw = fdrSector.getBytes();

        raw[FdrConstants.FDR_LEVEL3_RECORDS_USED]     = (byte) (count & 0xFF);
        raw[FdrConstants.FDR_LEVEL3_RECORDS_USED + 1] = (byte) ((count >> 8) & 0xFF);

        fdrSector.setData(raw);
    }

}
