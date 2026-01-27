package com.miriki.ti99.dskimg.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.miriki.ti99.dskimg.domain.constants.DiskLayoutConstants;
import com.miriki.ti99.dskimg.io.Cluster;
import com.miriki.ti99.dskimg.io.Sector;

/**
 * Represents a full TI‑99 disk image in memory.
 *
 * Low-level responsibilities only:
 *   - raw sector storage
 *   - sector allocation state
 *   - sector/cluster access
 *   - allocation of FDR and data sectors
 *   - writing directory entries (FDI)
 *
 * No high-level filesystem logic is implemented here.
 */
public final class Ti99Image {

    /** TI convention: erased sectors are filled with 0xE5. */
    private static final byte ERASED = (byte) 0xE5;

    /** System sectors: VIB (0) + FDI (1). */
    private static final int SYSTEM_SECTORS = 2;

    /** Raw disk data (size = totalSectors * 256). */
    private final byte[] data;

    /** Total number of sectors in this image. */
    private final int totalSectors;

    /** Allocation state: true = used, false = free. */
    private final boolean[] sectorUsed;

    /** Disk format describing geometry and layout. */
    private final DiskFormat format;

    // ============================================================
    //  CONSTRUCTORS
    // ============================================================

    /**
     * Creates a new, empty TI‑99 disk image.
     * All sectors are filled with 0xE5 except the first two bytes (TI convention).
     */
    public Ti99Image(DiskFormat format) {
        this.format = Objects.requireNonNull(format, "format must not be null");
        this.totalSectors = format.getTotalSectors();
        this.data = new byte[totalSectors * DiskLayoutConstants.SECTOR_SIZE];
        this.sectorUsed = new boolean[totalSectors];

        initializeEmptyImage();
    }

    /**
     * Creates a TI‑99 disk image from existing raw data.
     *
     * @param format disk format
     * @param data   raw sector data (must match expected size)
     */
    public Ti99Image(DiskFormat format, byte[] data) {
        this.format = Objects.requireNonNull(format, "format must not be null");
        Objects.requireNonNull(data, "data must not be null");

        int expected = format.getTotalSectors() * DiskLayoutConstants.SECTOR_SIZE;
        if (data.length != expected) {
            throw new IllegalArgumentException(
                    "Data length mismatch: expected " + expected + ", got " + data.length
            );
        }

        this.totalSectors = format.getTotalSectors();
        this.data = data;
        this.sectorUsed = new boolean[totalSectors];

        initializeUsedSectorsFromFormat();
    }

    // ============================================================
    //  INITIALIZATION HELPERS
    // ============================================================

    /**
     * Initializes a new empty image:
     *   - system sectors marked as used
     *   - all bytes filled with 0xE5
     *   - first two bytes zeroed (TI convention)
     */
    private void initializeEmptyImage() {
        for (int i = 0; i < SYSTEM_SECTORS; i++) {
            sectorUsed[i] = true;
        }

        for (int i = 0; i < data.length; i++) {
            data[i] = ERASED;
        }

        data[0] = 0;
        data[1] = 0;
    }

    /**
     * Initializes sectorUsed[] for an existing image.
     * TODO: reconstruct from VIB + FDI.
     */
    private void initializeUsedSectorsFromFormat() {
        for (int i = 0; i < SYSTEM_SECTORS; i++) {
            sectorUsed[i] = true;
        }
    }

    // ============================================================
    //  BASIC ACCESSORS
    // ============================================================

    public int getTotalSectors() {
        return totalSectors;
    }

    public DiskFormat getFormat() {
        return format;
    }

    /**
     * Returns the raw image data (mutable).
     * Callers must treat this as read-only.
     */
    public byte[] getRawData() {
        return data;
    }

    // ============================================================
    //  SECTOR / CLUSTER ACCESS
    // ============================================================

    public Sector getSector(int index) {
        checkSectorIndex(index);
        return new Sector(data, index);
    }

    public Cluster getCluster(int index) {
        checkClusterIndex(index);
        return new Cluster(format, data, index);
    }

    // ============================================================
    //  SECTOR USAGE
    // ============================================================

    public boolean isSectorFree(int sec) {
        return !sectorUsed[sec];
    }

    public void markSectorUsed(int sec) {
        sectorUsed[sec] = true;
    }

    public void markSectorFree(int sec) {
        sectorUsed[sec] = false;
    }

    // ============================================================
    //  ALLOCATION HELPERS
    // ============================================================

    /**
     * Allocates any free sectors in the data area.
     */
    public List<Integer> allocateSectors(int count) {
        return allocateInRange(format.getFirstDataSector(), totalSectors, count);
    }

    /**
     * Allocates one FDR sector.
     */
    public int allocateFdrSector() {
        List<Integer> result = allocateInRange(format.getFirstFdrSector(), format.getFirstDataSector(), 1);
        return result.get(0);
    }

    /**
     * Allocates data sectors only.
     */
    public List<Integer> allocateDataSectors(int count) {
        return allocateInRange(format.getFirstDataSector(), totalSectors, count);
    }

    /**
     * Allocates 'count' free sectors in the given range.
     */
    private List<Integer> allocateInRange(int start, int end, int count) {
        List<Integer> result = new ArrayList<>();

        for (int sec = start; sec < end && result.size() < count; sec++) {
            if (isSectorFree(sec)) {
                markSectorUsed(sec);
                result.add(sec);
            }
        }

        if (result.size() != count) {
            throw new IllegalStateException("Not enough free sectors");
        }

        return result;
    }

    // ============================================================
    //  DIRECTORY ENTRY CREATION (FDI)
    // ============================================================

    /**
     * Adds an entry to the FDI sector for the given file.
     * Searches the first two directory sectors for a free slot.
     */
    public void addDirectoryEntry(String name, int fdrSector) {

        for (int dirSec = 1; dirSec <= 2; dirSec++) {
            Sector s = getSector(dirSec);
            byte[] raw = s.getBytes();

            for (int i = 0; i < 8; i++) {
                int off = i * 2;

                if (raw[off] == 0x00 && raw[off + 1] == 0x00) {
                    raw[off]     = (byte) ((fdrSector >> 8) & 0xFF);
                    raw[off + 1] = (byte) (fdrSector & 0xFF);

                    s.setData(data);
                    return;
                }
            }
        }

        throw new IllegalStateException("No free directory entry in FDI");
    }

    // ============================================================
    //  INTERNAL VALIDATION
    // ============================================================

    private void checkSectorIndex(int index) {
        if (index < 0 || index >= totalSectors) {
            throw new IndexOutOfBoundsException("Sector " + index);
        }
    }

    private void checkClusterIndex(int index) {
        if (index < 0 || index >= format.getClusterCount()) {
            throw new IndexOutOfBoundsException("Cluster " + index);
        }
    }
}
