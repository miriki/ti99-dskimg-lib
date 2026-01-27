package com.miriki.ti99.dskimg.domain;

import java.util.Objects;

/**
 * Represents the TI‑99 Volume Information Block (VIB).
 *
 * Pure in-memory model of the decoded VIB sector.
 */
public final class VolumeInformationBlock {

    // ============================================================
    //  BASIC DISK INFORMATION
    // ============================================================

    private String volumeName;            // padded 10 chars
    private final int totalSectors;       // 16-bit
    private final int sectorsPerTrack;    // 8-bit
    private final int tracksPerSide;      // 8-bit
    private final int sides;              // 1 or 2
    private final int density;            // TI-specific

    // ============================================================
    //  SIGNATURE
    // ============================================================

    private final String signature;       // usually "DSK"

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

        this.volumeName = Objects.requireNonNull(volumeName).trim();
        this.totalSectors = totalSectors;
        this.sectorsPerTrack = sectorsPerTrack;
        this.tracksPerSide = tracksPerSide;
        this.sides = sides;
        this.density = density;

        this.signature = Objects.requireNonNull(signature);

        this.dir1Name = Objects.requireNonNull(dir1Name).trim();
        this.dir1Pointer = dir1Pointer;

        this.dir2Name = Objects.requireNonNull(dir2Name).trim();
        this.dir2Pointer = dir2Pointer;

        this.dir3Name = Objects.requireNonNull(dir3Name).trim();
        this.dir3Pointer = dir3Pointer;

        this.abm = Objects.requireNonNull(abm);
    }

    // ============================================================
    //  MUTATORS
    // ============================================================

    public void setVolumeName(String name) {
        this.volumeName = Objects.requireNonNull(name).trim();
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

    // ============================================================
    //  CONVENIENCE
    // ============================================================

    public boolean isDoubleSided() { return sides == 2; }
    public boolean isSingleSided() { return sides == 1; }

    public int getTotalTracks() {
        return tracksPerSide * sides;
    }

    /** Returns all directory pointers as a simple array. */
    public int[] getDirectoryPointers() {
        return new int[] { dir1Pointer, dir2Pointer, dir3Pointer };
    }

    /** Returns all directory names as a simple array. */
    public String[] getDirectoryNames() {
        return new String[] { dir1Name, dir2Name, dir3Name };
    }

    @Override
    public String toString() {
        return "VIB{" +
                "volumeName='" + volumeName + '\'' +
                ", totalSectors=" + totalSectors +
                ", sectorsPerTrack=" + sectorsPerTrack +
                ", tracksPerSide=" + tracksPerSide +
                ", sides=" + sides +
                ", density=" + density +
                ", signature='" + signature + '\'' +
                '}';
    }
}
