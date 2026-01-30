package com.miriki.ti99.dskimg.physical;

/**
 * Placeholder for future simulation of physical track layout.
 *
 * This class documents how a disk track is structured at the
 * magnetic level. It does not implement any logic yet.
 *
 * Topics to be covered:
 *
 * 1. Track structure:
 *    - Index hole alignment
 *    - Sector ordering (logical vs. physical)
 *    - Interleave patterns
 *    - Skew between tracks
 *
 * 2. Gap layout:
 *    - GAP1 (post-index)
 *    - GAP2 (between ID and data)
 *    - GAP3 (between sectors)
 *    - GAP4 (pre-index)
 *
 * 3. Controller-specific behavior:
 *    - HFDC: strict sector ordering
 *    - TI FDC: tolerant toward malformed tracks
 *    - BwG: custom interleave defaults
 *
 * 4. Copy protection relevance:
 *    - Duplicate sector IDs
 *    - Missing ID fields
 *    - Overlapping sectors
 *    - Sectors with impossible size codes
 *    - Tracks with more sectors than physically possible
 *
 * This class is a conceptual placeholder only.
 */
public final class TrackLayout {

    /** Placeholder for interleave patterns. */
    public enum Interleave {
        NONE,
        STANDARD,
        CUSTOM
    }

    // No fields, no logic — future extension point.
}
