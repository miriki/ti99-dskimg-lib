package com.miriki.ti99.imagetools.domain;

/**
 * Represents the physical geometry of a TI‑99 disk format.
 *
 * Parameters:
 *   - heads:            1 = single‑sided, 2 = double‑sided
 *   - tracksPerHead:    typically 40 or 80
 *   - sectorsPerTrack:  e.g. 9 (SSSD), 18 (DSDD), 36 (QD/HD)
 *   - bytesPerSector:   always 256 on TI‑99 systems
 *
 * This record is pure metadata and contains no IO logic.
 * It is used by DiskFormat to compute total capacity and layout.
 */
public record DiskGeometry(
        int heads,
        int tracksPerHead,
        int sectorsPerTrack,
        int bytesPerSector
) {

    // ============================================================
    //  VALIDATION
    // ============================================================

    public DiskGeometry {
        if (heads <= 0)
            throw new IllegalArgumentException("Heads must be > 0");
        if (tracksPerHead <= 0)
            throw new IllegalArgumentException("Tracks per head must be > 0");
        if (sectorsPerTrack <= 0)
            throw new IllegalArgumentException("Sectors per track must be > 0");
        if (bytesPerSector <= 0)
            throw new IllegalArgumentException("Bytes per sector must be > 0");
    }

    // ============================================================
    //  DERIVED VALUES
    // ============================================================

    /**
     * Returns the total number of sectors on the disk.
     */
    public int totalSectors() {
        return heads * tracksPerHead * sectorsPerTrack;
    }

    /**
     * Returns the total number of bytes on the disk.
     */
    public int totalBytes() {
        return totalSectors() * bytesPerSector;
    }
}
