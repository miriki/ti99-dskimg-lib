package com.miriki.ti99.dskimg.domain;

import java.util.Arrays;
import java.util.Objects;

import com.miriki.ti99.dskimg.domain.constants.VibConstants;

/**
 * Logical allocation bitmap for TI‑99 disk sectors.
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
 */
public final class AllocationBitmap {

    /** Maximum number of sectors representable in a TI‑99 ABM (200 bytes × 8 bits). */
    // private static final int MAX_BITMAP_SECTORS = VibConstants.MAX_BITMAP_SECTORS;

    /** Total number of sectors covered by this bitmap. */
    private final int totalSectors;

    /** Allocation state: true = used, false = free. */
    private final boolean[] used;

    /** Allocation state for sectors beyond totalSectors (always blocked). */
    private final boolean[] blocked;

    // ============================================================
    //  CONSTRUCTORS
    // ============================================================

    /** Creates a bitmap with all sectors initially free. */
    public AllocationBitmap(int totalSectors) {

        if (totalSectors <= 0) {
            throw new IllegalArgumentException("totalSectors must be > 0");
        }
        if (totalSectors > VibConstants.MAX_BITMAP_SECTORS) {
            throw new IllegalArgumentException("totalSectors exceeds TI‑99 ABM capacity");
        }

        this.totalSectors = totalSectors;
        this.used = new boolean[totalSectors];
        Arrays.fill(this.used, false);

        int blockedSize = VibConstants.MAX_BITMAP_SECTORS - totalSectors;
        this.blocked = new boolean[blockedSize];
        Arrays.fill(this.blocked, true);
    }

    /** Internal constructor used by fromBytes(). */
    private AllocationBitmap(int totalSectors, boolean[] used, boolean[] blocked) {
        this.totalSectors = totalSectors;
        this.used = used;
        this.blocked = blocked;
    }

    // ============================================================
    //  BASIC ACCESSORS
    // ============================================================

    public int getTotalSectors() {
        return totalSectors;
    }

    /** Number of usable sectors (same as totalSectors). */
    public int size() {
        return used.length;
    }

    // ============================================================
    //  ALLOCATION STATE
    // ============================================================

    public boolean isUsed(int sectorIndex) {
        checkRange(sectorIndex);
        return used[sectorIndex];
    }

    public void setUsed(int sectorIndex, boolean value) {
        checkRange(sectorIndex);
        used[sectorIndex] = value;
    }

    public void allocate(int sectorIndex) {
        setUsed(sectorIndex, true);
    }

    public void free(int sectorIndex) {
        setUsed(sectorIndex, false);
    }

    public void freeCluster(DiskFormat fmt, int cluster) {
        int first = fmt.clusterToSector(cluster);
        int count = fmt.getSectorsPerCluster();
        for (int i = 0; i < count; i++) {
            setUsed(first + i, false);
        }
    }

    /**
     * Finds the first free sector, marks it used, and returns its index.
     * Returns -1 if no free sector exists.
     */
    public int allocateFirstFree() {
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

    /** Serializes the bitmap into packed bytes (LSB-first). */
    public byte[] toBytes() {

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

        return Arrays.copyOf(result, result.length);
    }

    /**
     * Deserializes a packed bitmap into an AllocationBitmap.
     */
    public static AllocationBitmap fromBytes(byte[] bytes, int totalSectors) {

        Objects.requireNonNull(bytes, "bytes must not be null");

        if (totalSectors <= 0) {
            throw new IllegalArgumentException("totalSectors must be > 0");
        }
        if (totalSectors > VibConstants.MAX_BITMAP_SECTORS) {
            throw new IllegalArgumentException("totalSectors exceeds TI‑99 ABM capacity");
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

        boolean[] blocked = new boolean[VibConstants.MAX_BITMAP_SECTORS - totalSectors];
        Arrays.fill(blocked, true);

        return new AllocationBitmap(totalSectors, used, blocked);
    }

    // ============================================================
    //  FACTORY HELPERS
    // ============================================================

    public static AllocationBitmap createAllFree(int totalSectors) {
        return new AllocationBitmap(totalSectors);
    }

    public static AllocationBitmap createAllUsed(int totalSectors) {

        boolean[] used = new boolean[totalSectors];
        Arrays.fill(used, true);

        boolean[] blocked = new boolean[VibConstants.MAX_BITMAP_SECTORS - totalSectors];
        Arrays.fill(blocked, true);

        return new AllocationBitmap(totalSectors, used, blocked);
    }

    // ============================================================
    //  INTERNAL RANGE CHECK
    // ============================================================

    private void checkRange(int sectorIndex) {
        if (sectorIndex < 0 || sectorIndex >= totalSectors) {
            throw new IndexOutOfBoundsException(
                    "sectorIndex " + sectorIndex + " out of range 0.." + (totalSectors - 1)
            );
        }
    }

    // ============================================================
    //  DEBUG STRING
    // ============================================================

    @Override
    public String toString() {
        return "AllocationBitmap{" +
                "totalSectors=" + totalSectors +
                ", used=" + Arrays.toString(used) +
                '}';
    }
}
