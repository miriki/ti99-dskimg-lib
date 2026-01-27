package com.miriki.ti99.dskimg.domain;

/**
 * Represents the physical geometry of a TI‑99 disk format.
 *
 * Pure metadata, no IO logic.
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

        // Optional TI‑99 constraints:
        // if (heads > 2) throw new IllegalArgumentException("TI‑99 disks have max 2 heads");
        // if (bytesPerSector != 256) throw new IllegalArgumentException("TI‑99 uses 256‑byte sectors");
    }

    // ============================================================
    //  DERIVED VALUES
    // ============================================================

    /** Total number of sectors on the disk. */
    public int totalSectors() {
        return heads * tracksPerHead * sectorsPerTrack;
    }

    /** Total number of bytes on the disk. */
    public long totalBytes() {
        return (long) totalSectors() * bytesPerSector;
    }

    /** Total number of tracks across all heads. */
    public int totalTracks() {
        return heads * tracksPerHead;
    }

    /** True if the disk is single‑sided. */
    public boolean isSingleSided() {
        return heads == 1;
    }

    /** True if the disk is double‑sided. */
    public boolean isDoubleSided() {
        return heads == 2;
    }

    @Override
    public String toString() {
        return "DiskGeometry{" +
                "heads=" + heads +
                ", tracksPerHead=" + tracksPerHead +
                ", sectorsPerTrack=" + sectorsPerTrack +
                ", bytesPerSector=" + bytesPerSector +
                '}';
    }
}
