package com.miriki.ti99.imagetools.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.miriki.ti99.imagetools.domain.AllocationBitmap;

/**
 * IO helper for reading and writing the Allocation Bitmap (ABM)
 * stored inside the Volume Information Block (VIB).
 *
 * The ABM marks which sectors of the disk are allocated (1) or free (0).
 *
 * Layout inside the VIB:
 *   OFFSET = 56 (0x38)
 *   Each bit represents one sector:
 *     bit = 1 → sector allocated
 *     bit = 0 → sector free
 *
 * The bitmap length depends on the total number of sectors:
 *   bytes = ceil(totalSectors / 8)
 */
public final class AllocationBitmapIO {

    private static final Logger log = LoggerFactory.getLogger(AllocationBitmapIO.class);

    /** Byte offset of the Allocation Bitmap inside the VIB */
    private static final int OFFSET = 56;

    /** Size of a TI sector */
    private static final int SECTOR_SIZE = 256;

    private AllocationBitmapIO() {
        // Utility class – no instances
    }

    // ============================================================
    //  PUBLIC API – READ
    // ============================================================

    /**
     * Reads the Allocation Bitmap (ABM) from the given VIB sector.
     *
     * @param vibSector     the VIB sector
     * @param totalSectors  total number of sectors on the disk
     * @return AllocationBitmap domain object
     */
    public static AllocationBitmap read(Sector vibSector, int totalSectors) {
        log.debug("read(vibSector={}, totalSectors={})", vibSector, totalSectors);

        byte[] raw = vibSector.getBytes();
        AllocationBitmap abm = new AllocationBitmap(totalSectors);

        // Check each sector bit
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

    /**
     * Writes the Allocation Bitmap (ABM) back into the VIB sector.
     *
     * @param vibSector the VIB sector to update
     * @param abm       the AllocationBitmap domain object
     */
    public static void write(Sector vibSector, AllocationBitmap abm) {
        log.debug("write(vibSector={}, abm={})", vibSector, abm);

        byte[] raw = vibSector.getBytes();
        int total = abm.size();

        clearBitmap(raw, total);
        writeBitmap(raw, abm, total);
        writeBackToSector(vibSector, raw);
    }

    // ============================================================
    //  INTERNAL HELPERS – BIT OPERATIONS
    // ============================================================

    /**
     * Returns true if the given sector is marked as allocated.
     */
    private static boolean isSectorUsed(byte[] raw, int sectorIndex) {
        int byteIndex = OFFSET + (sectorIndex / 8);
        int bitIndex  = sectorIndex % 8;
        return (raw[byteIndex] & (1 << bitIndex)) != 0;
    }

    /**
     * Sets the bit for the given sector to 1 (allocated).
     */
    private static void setSectorUsed(byte[] raw, int sectorIndex) {
        int byteIndex = OFFSET + (sectorIndex / 8);
        int bitIndex  = sectorIndex % 8;
        raw[byteIndex] |= (1 << bitIndex);
    }
    /*
    private static void setSectorUsed(byte[] raw, int sectorIndex) {
        if (sectorIndex <= 0) {
            throw new IllegalArgumentException("sectorIndex must be >= 1");
        }
        int idx = sectorIndex - 1;
        int byteIndex = OFFSET + (idx / 8);
        int bitIndex  = idx % 8;
        raw[byteIndex] |= (1 << bitIndex);
    }
    */

    /**
     * Clears the bitmap area inside the VIB.
     */
    private static void clearBitmap(byte[] raw, int totalSectors) {
        int bytes = (totalSectors + 7) / 8; // round up
        for (int i = 0; i < bytes; i++) {
            raw[OFFSET + i] = 0;
        }
    }

    /**
     * Writes all allocated sectors into the bitmap.
     */
    private static void writeBitmap(byte[] raw, AllocationBitmap abm, int totalSectors) {
        for (int sector = 0; sector < totalSectors; sector++) {
            if (abm.isUsed(sector)) {
                setSectorUsed(raw, sector);
            }
        }
    }

    /**
     * Writes the modified VIB sector back to disk.
     */
    private static void writeBackToSector(Sector vibSector, byte[] raw) {
        for (int i = 0; i < SECTOR_SIZE; i++) {
            vibSector.set(i, raw[i]);
        }
    }
}
