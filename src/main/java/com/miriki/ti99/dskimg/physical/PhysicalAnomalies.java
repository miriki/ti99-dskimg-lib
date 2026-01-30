package com.miriki.ti99.dskimg.physical;

/**
 * Placeholder for future simulation of physical disk anomalies.
 *
 * This class does not implement any logic yet. It serves as a
 * documentation anchor for planned features related to low-level
 * disk behavior, such as:
 *
 * 1. Bad sectors (unreadable or unwritable)
 * 2. Weak bits / fuzzy sectors (read returns different data each time)
 * 3. Illegal or malformed ID marks
 * 4. Duplicate sector IDs on the same track
 * 5. Sectors with non-standard sizes (e.g. 1024 bytes)
 * 6. Sectors that are readable but not writable
 * 7. Sectors that only succeed after multiple read attempts
 * 8. Timing-based anomalies (e.g. artificially long seek times)
 *
 * These features are historically relevant for:
 *  - copy protection schemes
 *  - controller diagnostics
 *  - forensic disk analysis
 *  - emulation of real-world hardware quirks
 *
 * The simulation layer will be optional and disabled by default.
 * It will operate below the logical filesystem layer and will
 * integrate with the IO subsystem (sector read/write operations).
 *
 * Nothing in this class is final. It is a conceptual placeholder.
 */
public final class PhysicalAnomalies {

    /**
     * Enumeration of possible anomaly types.
     * This is intentionally incomplete and may be expanded later.
     */
    public enum Type {
        BAD_SECTOR,
        WEAK_SECTOR,
        ILLEGAL_ID_MARK,
        DUPLICATE_ID,
        NONSTANDARD_SIZE,
        READ_ONLY_SECTOR,
        RETRY_REQUIRED,
        TIMING_ANOMALY
    }

    // No fields, no logic — this is a future extension point.
}
