package com.miriki.ti99.dskimg.io;

import java.util.Objects;

import com.miriki.ti99.dskimg.domain.AllocationBitmap;
import com.miriki.ti99.dskimg.domain.constants.VibConstants;

/**
 * Reads and writes the Allocation Bitmap (ABM) stored inside the
 * Volume Information Block (VIB).
 *
 * Bit order is LSB-first within each byte (TI DOS convention).
 */
public final class AllocationBitmapIO {

    private AllocationBitmapIO() {}

    // ============================================================
    //  PUBLIC API – READ
    // ============================================================

    public static AllocationBitmap read(Sector vibSector, int totalSectors) {
        Objects.requireNonNull(vibSector, "vibSector must not be null");

        byte[] raw = vibSector.getBytes();
        AllocationBitmap abm = new AllocationBitmap(totalSectors);

        for (int sector = 0; sector < totalSectors; sector++) {
            if (isSectorUsed(raw, sector)) {
                abm.allocate(sector);
            }
        }

        return abm;
    }

    // ============================================================
    //  PUBLIC API – WRITE
    // ============================================================

    public static void write(Sector vibSector, AllocationBitmap abm) {
        Objects.requireNonNull(vibSector, "vibSector must not be null");
        Objects.requireNonNull(abm, "abm must not be null");

        byte[] raw = vibSector.getBytes();
        int total = abm.size();

        clearBitmap(raw, total);
        writeBitmap(raw, abm, total);

        vibSector.setData(raw);
    }

    // ============================================================
    //  INTERNAL HELPERS – BIT OPERATIONS
    // ============================================================

    private static boolean isSectorUsed(byte[] raw, int sectorIndex) {
        int byteIndex = VibConstants.VIB_ABM_OFFSET + (sectorIndex / 8);
        int bitIndex  = sectorIndex % 8;
        return (raw[byteIndex] & (1 << bitIndex)) != 0;
    }

    private static void setSectorUsed(byte[] raw, int sectorIndex) {
        int byteIndex = VibConstants.VIB_ABM_OFFSET + (sectorIndex / 8);
        int bitIndex  = sectorIndex % 8;
        raw[byteIndex] |= (1 << bitIndex);
    }

    private static void clearBitmap(byte[] raw, int totalSectors) {
        int bytes = (totalSectors + 7) / 8;
        for (int i = 0; i < bytes; i++) {
            raw[VibConstants.VIB_ABM_OFFSET + i] = 0;
        }
    }

    private static void writeBitmap(byte[] raw, AllocationBitmap abm, int totalSectors) {
        for (int sector = 0; sector < totalSectors; sector++) {
            if (abm.isUsed(sector)) {
                setSectorUsed(raw, sector);
            }
        }
    }
}
