package com.miriki.ti99.dskimg.physical;

/**
 * Placeholder for future simulation of TI-99/4A disk controller behavior.
 *
 * This class documents how real hardware controllers (TI, HFDC, BwG,
 * CorComp, Myarc, etc.) behave when interacting with physical disk
 * anomalies or unusual sector layouts.
 *
 * It does not implement any logic yet. It serves as a conceptual
 * anchor for future extensions.
 *
 * Topics to be covered:
 *
 * 1. Error codes returned by different controllers:
 *    - CRC error
 *    - Lost data
 *    - Missing address mark
 *    - Seek error
 *    - Write fault
 *    - Record not found
 *
 * 2. Retry behavior:
 *    - Number of automatic retries
 *    - Timing between retries
 *    - Conditions for giving up
 *
 * 3. Controller-specific quirks:
 *    - HFDC: strict ID mark validation
 *    - TI FDC: tolerant toward malformed sectors
 *    - BwG: custom track stepping logic
 *    - CorComp: different error code mapping
 *
 * 4. Timing behavior:
 *    - Seek time simulation
 *    - Rotational latency
 *    - Track-to-track stepping delays
 *
 * 5. Interaction with physical anomalies:
 *    - How controllers react to bad sectors
 *    - How they handle weak/fuzzy sectors
 *    - How they interpret duplicate ID fields
 *    - How they behave with non-standard sector sizes
 *
 * 6. Optional advanced simulation:
 *    - Emulating real-world "flaky" drives
 *    - Random intermittent read/write failures
 *    - Temperature-related behavior (purely for fun)
 *
 * This simulation layer will be optional and disabled by default.
 * It will integrate with the low-level IO subsystem and may be
 * used for:
 *   - copy protection emulation
 *   - forensic disk analysis
 *   - retro hardware research
 *   - advanced debugging
 */
public final class ControllerBehavior {

    /**
     * Enumeration of controller error codes.
     * These are placeholders and will be refined later.
     */
    public enum ErrorCode {
        CRC_ERROR,
        LOST_DATA,
        MISSING_ADDRESS_MARK,
        SEEK_ERROR,
        WRITE_FAULT,
        RECORD_NOT_FOUND
    }

    // No fields, no logic — this is a future extension point.
}
