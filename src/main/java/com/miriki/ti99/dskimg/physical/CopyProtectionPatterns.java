package com.miriki.ti99.dskimg.physical;

/**
 * Placeholder for future catalog of copy protection patterns.
 *
 * This class documents known or typical protection schemes used
 * on TI-99/4A and related systems. It does not implement any logic yet.
 *
 * Topics to be covered:
 *
 * 1. Weak/fuzzy sectors
 * 2. Intentional CRC errors
 * 3. Duplicate sector IDs
 * 4. Non-standard sector sizes
 * 5. Tracks with impossible geometry
 * 6. Sectors readable but not writable
 * 7. Timing-based protections
 *
 * This catalog will later be used for:
 *   - emulation
 *   - forensic analysis
 *   - disk imaging tools
 *   - educational purposes
 */
public final class CopyProtectionPatterns {

    /** Placeholder for known pattern types. */
    public enum Pattern {
        WEAK_SECTOR,
        CRC_ERROR,
        DUPLICATE_ID,
        NONSTANDARD_SIZE,
        READ_ONLY_SECTOR,
        TIMING_BASED
    }

    // No fields, no logic — future extension point.
}
