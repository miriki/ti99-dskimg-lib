package com.miriki.ti99.dskimg.physical;

/**
 * Placeholder for future simulation of sector encoding formats.
 *
 * This class documents how raw magnetic data is encoded on disk
 * surfaces. It does not implement any logic yet.
 *
 * Topics to be covered:
 *
 * 1. Encoding schemes:
 *    - FM (single density)
 *    - MFM (double density)
 *    - GCR (group-coded recording)
 *
 * 2. Structure of a physical sector:
 *    - GAP1 (post-index gap)
 *    - IDAM (ID address mark)
 *    - ID field (track, side, sector, size)
 *    - CRC for ID field
 *    - GAP2
 *    - DAM (data address mark)
 *    - Data field
 *    - CRC for data field
 *    - GAP3 (inter-sector gap)
 *
 * 3. Controller interpretation:
 *    - How HFDC parses IDAM/DAM
 *    - How TI FDC handles missing address marks
 *    - How BwG/CorComp differ in tolerance
 *
 * 4. Copy protection relevance:
 *    - Illegal DAM values
 *    - Missing CRC bytes
 *    - Overlapping sectors
 *    - Sectors with mismatched size codes
 *
 * This class is a conceptual placeholder only.
 */
public final class SectorEncoding {

    /** Supported encoding types (placeholder). */
    public enum Type {
        FM,
        MFM,
        GCR
    }

    // No fields, no logic — future extension point.
}
