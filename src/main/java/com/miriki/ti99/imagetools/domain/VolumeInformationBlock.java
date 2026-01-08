package com.miriki.ti99.imagetools.domain;

import java.util.Objects;

/**
 * Represents the TI‑99 Volume Information Block (VIB).
 *
 * The VIB is the first sector of a TI‑99 disk and contains:
 *
 *   - basic disk geometry (tracks, sides, density)
 *   - volume name
 *   - directory pointers (3 directory entries)
 *   - allocation bitmap (ABM)
 *   - signature ("DSK")
 *
 * This class is a pure in-memory representation of the decoded VIB.
 * It contains no IO logic; reading/writing is handled by VIBReader/VIBWriter.
 */
public final class VolumeInformationBlock {

    // ============================================================
    //  BASIC DISK INFORMATION
    // ============================================================

    /** Volume name (10 characters, padded). */
    private String volumeName;

    /** Total number of sectors on the disk (16-bit). */
    private final int totalSectors;

    /** Sectors per track (8-bit). */
    private final int sectorsPerTrack;

    /** Tracks per side (8-bit). */
    private final int tracksPerSide;

    /** Number of sides (1 or 2). */
    private final int sides;

    /** Density code (TI-specific). */
    private final int density;

    // ============================================================
    //  SIGNATURE
    // ============================================================

    /** Always "DSK" for TI‑99 disks. */
    private final String signature;

    // ============================================================
    //  DIRECTORY POINTERS (3 ENTRIES)
    // ============================================================

    private final String dir1Name;
    private final int dir1Pointer;

    private final String dir2Name;
    private final int dir2Pointer;

    private final String dir3Name;
    private final int dir3Pointer;

    // ============================================================
    //  ALLOCATION BITMAP
    // ============================================================

    /** Logical allocation bitmap (size depends on disk format). */
    private final AllocationBitmap abm;

    // ============================================================
    //  CONSTRUCTOR
    // ============================================================

    public VolumeInformationBlock(
            String volumeName,
            int totalSectors,
            int sectorsPerTrack,
            int tracksPerSide,
            int sides,
            int density,
            String signature,
            String dir1Name,
            int dir1Pointer,
            String dir2Name,
            int dir2Pointer,
            String dir3Name,
            int dir3Pointer,
            AllocationBitmap abm) {

        this.volumeName = Objects.requireNonNull(volumeName, "volumeName must not be null");
        this.totalSectors = totalSectors;
        this.sectorsPerTrack = sectorsPerTrack;
        this.tracksPerSide = tracksPerSide;
        this.sides = sides;
        this.density = density;
        this.signature = Objects.requireNonNull(signature, "signature must not be null");

        this.dir1Name = Objects.requireNonNull(dir1Name, "dir1Name must not be null");
        this.dir1Pointer = dir1Pointer;

        this.dir2Name = Objects.requireNonNull(dir2Name, "dir2Name must not be null");
        this.dir2Pointer = dir2Pointer;

        this.dir3Name = Objects.requireNonNull(dir3Name, "dir3Name must not be null");
        this.dir3Pointer = dir3Pointer;

        this.abm = Objects.requireNonNull(abm, "AllocationBitmap must not be null");
    }

    // ============================================================
    //  MUTATORS
    // ============================================================

    /**
     * Sets the volume name.
     * (The VIB stores this as a padded 10‑byte field.)
     */
    public void setVolumeName(String name) {
        this.volumeName = Objects.requireNonNull(name, "volumeName must not be null");
    }

    // ============================================================
    //  GETTERS
    // ============================================================

    public String getVolumeName() { return volumeName; }
    public int getTotalSectors() { return totalSectors; }
    public int getSectorsPerTrack() { return sectorsPerTrack; }
    public int getTracksPerSide() { return tracksPerSide; }
    public int getSides() { return sides; }
    public int getDensity() { return density; }
    public String getSignature() { return signature; }

    public String getDir1Name() { return dir1Name; }
    public int getDir1Pointer() { return dir1Pointer; }

    public String getDir2Name() { return dir2Name; }
    public int getDir2Pointer() { return dir2Pointer; }

    public String getDir3Name() { return dir3Name; }
    public int getDir3Pointer() { return dir3Pointer; }

    public AllocationBitmap getAbm() { return abm; }
}
