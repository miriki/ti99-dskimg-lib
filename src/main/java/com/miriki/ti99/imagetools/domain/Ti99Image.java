package com.miriki.ti99.imagetools.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.miriki.ti99.imagetools.io.Cluster;
import com.miriki.ti99.imagetools.io.Sector;

/**
 * Represents a full TI‑99 disk image in memory.
 *
 * Responsibilities:
 *   - hold raw sector data
 *   - track sector allocation state
 *   - provide sector/cluster access
 *   - support allocation of FDR and data sectors
 *   - manage directory entries (FDI)
 *
 * This class contains no high-level filesystem logic; it is a low-level
 * container used by readers/writers and the Ti99FileSystem model.
 */
public final class Ti99Image {

    private static final Logger log = LoggerFactory.getLogger(Ti99Image.class);

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
        // log.info("[constructor] Ti99Image({})", format);

        this.format = Objects.requireNonNull(format);
        this.totalSectors = format.getTotalSectors();
        this.data = new byte[totalSectors * Sector.SIZE];
        this.sectorUsed = new boolean[totalSectors];

        initializeEmptyImage();
        log.info("  image created with {} bytes", data.length);
    }

    /**
     * Creates a TI‑99 disk image from existing raw data.
     *
     * @param format disk format
     * @param data   raw sector data (must match expected size)
     */
    public Ti99Image(DiskFormat format, byte[] data) {
        // log.info("[constructor] Ti99Image({}, data[{}])", format, data.length);

        this.format = Objects.requireNonNull(format);
        Objects.requireNonNull(data);

        int expected = format.getTotalSectors() * Sector.SIZE;
        if (data.length != expected) {
            throw new IllegalArgumentException(
                    "Data length mismatch: expected " + expected + ", got " + data.length
            );
        }

        this.totalSectors = format.getTotalSectors();
        this.data = data;
        this.sectorUsed = new boolean[totalSectors];

        initializeUsedSectorsFromFormat();
        log.info("  image created with {} bytes using existing data", data.length);
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
        // log.debug("getTotalSectors()");
        return totalSectors;
    }

    public DiskFormat getFormat() {
        // log.debug("getFormat()");
        return format;
    }

    /**
     * Returns the raw image data (mutable).
     * Callers must treat this as read-only.
     */
    public byte[] getRawData() {
        // log.debug("getRawData()");
        return data;
    }

    // ============================================================
    //  SECTOR / CLUSTER ACCESS
    // ============================================================

    public Sector getSector(int index) {
        // log.debug("getSector({})", index);
        checkSectorIndex(index);
        return new Sector(data, index);
    }

    public Cluster getCluster(int index) {
        // log.debug("getCluster({})", index);
        checkClusterIndex(index);
        return new Cluster(format, data, index);
    }

    // ============================================================
    //  SECTOR USAGE
    // ============================================================

    public boolean isSectorFree(int sec) {
        // log.debug("isSectorFree({})", sec);
        return !sectorUsed[sec];
    }

    public void markSectorUsed(int sec) {
        // log.debug("markSectorUsed({})", sec);
        sectorUsed[sec] = true;
    }

    public void markSectorFree(int sec) {
        // log.debug("markSectorFree({})", sec);
        sectorUsed[sec] = false;
    }

    // ============================================================
    //  ALLOCATION HELPERS
    // ============================================================

    /**
     * Allocates any free sectors in the data area.
     */
    public List<Integer> allocateSectors(int count) {
        // log.debug("allocateSectors({})", count);
        return allocateInRange(format.getFirstDataSector(), totalSectors, count);
    }

    /**
     * Allocates one FDR sector.
     */
    public int allocateFdrSector() {
        // log.debug("allocateFdrSector()");
        List<Integer> result = allocateInRange(format.getFirstFdrSector(), format.getFirstDataSector(), 1);
        return result.get(0);
    }

    /**
     * Allocates data sectors only.
     */
    public List<Integer> allocateDataSectors(int count) {
        // log.debug("allocateDataSectors({})", count);
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

        log.info("  {} sectors allocated in range {}..{}", result.size(), start, end - 1);
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
        // log.info("addDirectoryEntry({}, {})", name, fdrSector);

        for (int dirSec = 1; dirSec <= 2; dirSec++) {
            Sector s = getSector(dirSec);
            byte[] raw = s.getBytes();

            for (int i = 0; i < 8; i++) {
                int off = i * 2;

                if (raw[off] == 0x00 && raw[off + 1] == 0x00) {
                    raw[off]     = (byte) ((fdrSector >> 8) & 0xFF);
                    raw[off + 1] = (byte) (fdrSector & 0xFF);

                    s.setData(data);

                    log.info("  FDI entry created for '{}' -> FDR @ sector {}", name, fdrSector);
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
