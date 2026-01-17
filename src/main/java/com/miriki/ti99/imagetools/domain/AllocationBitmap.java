package com.miriki.ti99.imagetools.domain;

import java.util.Arrays;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logical allocation bitmap for TI-99 disk sectors.
 *
 * Semantics:
 *   - bit = 0 → sector free
 *   - bit = 1 → sector used
 *
 * Packing convention (LSB-first):
 *   sector 0 → byte[0], bit 0
 *   sector 1 → byte[0], bit 1
 *   ...
 *   sector 7 → byte[0], bit 7
 *   sector 8 → byte[1], bit 0
 *
 * This class provides:
 *   - immutable total sector count
 *   - mutable allocation state
 *   - conversion to/from packed byte representation
 */
public final class AllocationBitmap {

    @SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(AllocationBitmap.class);

    /** Total number of sectors covered by this bitmap. */
    private final int totalSectors;

    /** Allocation state: true = used, false = free. */
    private final boolean[] used;

    /** Allocation state: true = blocked, false = available. */
    private final boolean[] blocked;

    // ============================================================
    //  CONSTRUCTORS
    // ============================================================

    /**
     * Creates a bitmap with all sectors initially free.
     */
    public AllocationBitmap(int totalSectors) {
        // log.debug("[constructor] AllocationBitmap({})", totalSectors);

        if (totalSectors <= 0) {
            throw new IllegalArgumentException("totalSectors must be > 0");
        }

        this.totalSectors = totalSectors;
        this.used = new boolean[totalSectors]; // all free by default
        Arrays.fill(this.used, false);

        int blockedSize = 1600 - totalSectors;
        this.blocked = new boolean[blockedSize];
        Arrays.fill(this.blocked, true);
    }

    /**
     * Internal constructor used by fromBytes().
     */
    private AllocationBitmap(int totalSectors, boolean[] used, boolean[] blocked) {
        // log.debug("[constructor] AllocationBitmap({}, used[])", totalSectors);

        this.totalSectors = totalSectors;
        this.used = used;
        this.blocked = blocked;

        // int blockedSize = 1600 - totalSectors;
        // this.blocked = new boolean[blockedSize];
        // Arrays.fill(this.blocked, true);
    }

    // ============================================================
    //  BASIC ACCESSORS
    // ============================================================

    public int getTotalSectors() {
        // log.debug("getTotalSectors()");
        return totalSectors;
    }

    public int size() {
        // log.debug("size()");
        return used.length;
    }

    // ============================================================
    //  ALLOCATION STATE
    // ============================================================

    public boolean isUsed(int sectorIndex) {
        // log.debug("isUsed({})", sectorIndex);
        checkRange(sectorIndex);
        return used[sectorIndex];
    }

    public void setUsed(int sectorIndex, boolean value) {
        // log.debug("setUsed({}, {})", sectorIndex, value);
        checkRange(sectorIndex);
        used[sectorIndex] = value;
    }

    /** Marks a sector as used. */
    public void allocate(int sectorIndex) {
        // log.debug("allocate({})", sectorIndex);
        setUsed(sectorIndex, true);
    }

    /** Marks a sector as free. */
    public void free(int sectorIndex) {
        // log.debug("free({})", sectorIndex);
        setUsed(sectorIndex, false);
    }

    /**
     * Finds the first free sector, marks it used, and returns its index.
     * Returns -1 if no free sector exists.
     */
    public int allocateFirstFree() {
        // log.debug("allocateFirstFree()");

        for (int i = 0; i < totalSectors; i++) {
            if (!used[i]) {
                used[i] = true;
                return i;
            }
        }
        return -1;
    }

    // ============================================================
    //  SERIALIZATION
    // ============================================================

    /**
     * Serializes the bitmap into packed bytes (LSB-first).
     */
    public byte[] toBytes() {
        // log.debug("toBytes()");

        // int bytes = (totalSectors + 7) / 8;
        byte[] result = new byte[200];

        // used[]
        for (int sector = 0; sector < totalSectors; sector++) {
            if (used[sector]) {
                int byteIndex = sector / 8;
                int bitIndex  = sector % 8;
                result[byteIndex] |= (1 << bitIndex);
            }
        }
        
        // blocked[]
        for (int i = 0; i < blocked.length; i++) {
            if (blocked[i]) {
            	int sector = totalSectors + i;
                int byteIndex = sector / 8;
                int bitIndex  = sector % 8;
                result[byteIndex] |= (1 << bitIndex);
            }
        }

        return result;
    }

    /**
     * Deserializes a packed bitmap into an AllocationBitmap.
     *
     * @param bytes packed bitmap bytes (must contain at least ceil(totalSectors/8))
     * @param totalSectors number of sectors represented
     */
    public static AllocationBitmap fromBytes(byte[] bytes, int totalSectors) {
        // log.debug("fromBytes(bytes[{}], totalSectors={})", bytes.length, totalSectors);

        Objects.requireNonNull(bytes, "bytes must not be null");
        if (totalSectors <= 0) {
            throw new IllegalArgumentException("totalSectors must be > 0");
        }

        int needed = (totalSectors + 7) / 8;
        if (bytes.length < needed) {
            throw new IllegalArgumentException(
                    "Bitmap bytes too short: need " + needed + ", got " + bytes.length
            );
        }

        boolean[] used = new boolean[totalSectors];
        for (int sector = 0; sector < totalSectors; sector++) {
            int byteIndex = sector / 8;
            int bitIndex  = sector % 8;
            used[sector] = (bytes[byteIndex] & (1 << bitIndex)) != 0;
        }
        
        boolean[] blocked = new boolean[1600 - totalSectors];
        Arrays.fill(blocked, true);

        return new AllocationBitmap(totalSectors, used, blocked);
    }

    // ============================================================
    //  FACTORY HELPERS
    // ============================================================

    /** Creates a bitmap with all sectors free. */
    public static AllocationBitmap createAllFree(int totalSectors) {
        // log.debug("createAllFree({})", totalSectors);
        return new AllocationBitmap(totalSectors);
    }

    /** Creates a bitmap with all sectors marked as used. */
    public static AllocationBitmap createAllUsed(int totalSectors) {
        // log.debug("createAllUsed({})", totalSectors);

        boolean[] used = new boolean[totalSectors];
        Arrays.fill(used, true);

        boolean[] blocked = new boolean[1600 - totalSectors];
        Arrays.fill(blocked, true);

        return new AllocationBitmap(totalSectors, used, blocked);
    }

    // ============================================================
    //  INTERNAL RANGE CHECK
    // ============================================================

    private void checkRange(int sectorIndex) {
        // log.debug("checkRange({})", sectorIndex);

        if (sectorIndex < 0 || sectorIndex >= totalSectors) {
            throw new IndexOutOfBoundsException(
                    "sectorIndex " + sectorIndex + " out of range 0.." + (totalSectors - 1)
            );
        }
    }
}
