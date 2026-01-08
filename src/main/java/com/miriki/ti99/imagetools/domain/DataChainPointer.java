package com.miriki.ti99.imagetools.domain;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.miriki.ti99.imagetools.dto.DataChainPointerDTO;

/**
 * Represents a single HFDC Data Chain Pointer (DCP) entry.
 *
 * A DCP entry is exactly 3 bytes and encodes:
 *
 *   Byte 0: n2 n1  (low 8 bits of start sector)
 *   Byte 1: m1 n3  (high 4 bits of sector count, low 4 bits of start sector)
 *   Byte 2: m3 m2  (high 8 bits of sector count)
 *
 * Meaning:
 *   - n0 = start sector (12 bits)
 *   - m0 = sector count (12 bits)
 *
 * If m0 == 0, TI-DOS interprets this as "1 cluster".
 */
public final class DataChainPointer {

    private static final Logger log = LoggerFactory.getLogger(DataChainPointer.class);

    /** Raw 3-byte DCP entry. */
    private final byte[] raw;

    // ============================================================
    //  CONSTRUCTOR
    // ============================================================

    /**
     * Creates a DataChainPointer from a raw 3-byte array.
     *
     * @param raw exactly 3 bytes
     */
    public DataChainPointer(byte[] raw) {
        log.debug("[constructor] DataChainPointer({})", raw);

        if (raw.length != 3) {
            throw new IllegalArgumentException("Data chain pointer must be exactly 3 bytes");
        }

        // Defensive copy
        this.raw = Arrays.copyOf(raw, raw.length);
    }

    // ============================================================
    //  BASIC ACCESSOR
    // ============================================================

    /**
     * Returns a defensive copy of the raw 3-byte DCP entry.
     */
    public byte[] getRaw() {
        log.debug("getRaw()");
        return Arrays.copyOf(raw, raw.length);
    }

    // ============================================================
    //  DECODED FIELDS
    // ============================================================

    /**
     * Returns the first sector (n0).
     *
     * Layout:
     *   n0 = (n3 << 8) | n2n1
     */
    public int getFirstSector() {
        log.debug("getFirstSector()");

        int n2n1 = raw[0] & 0xFF;
        int m1n3 = raw[1] & 0xFF;

        return ((m1n3 & 0x0F) << 8) | n2n1;
    }

    /**
     * Returns the sector count (m0).
     *
     * Layout:
     *   m0 = ((m3m2 << 8) | (m1n3 & 0xF0)) >> 4
     */
    public int getSectorCount() {
        log.debug("getSectorCount()");

        int m1n3 = raw[1] & 0xFF;
        int m3m2 = raw[2] & 0xFF;

        return ((m3m2 << 8) | (m1n3 & 0xF0)) >> 4;
    }

    /**
     * Returns the last sector (n0 + m0 - 1).
     *
     * Special case:
     *   TI-DOS: m0 == 0 → treat as 1 cluster
     */
    public int getLastSector() {
        log.debug("getLastSector()");

        int first = getFirstSector();
        int count = getSectorCount();

        if (count == 0) {
            return first; // TI-DOS quirk
        }

        return first + count - 1;
    }

    // ============================================================
    //  DTO CONVERSION
    // ============================================================

    public DataChainPointerDTO toDTO() {
        log.debug("toDTO()");
        return new DataChainPointerDTO(raw);
    }

    public static DataChainPointer fromDTO(DataChainPointerDTO dto) {
        log.debug("fromDTO({})", dto);
        return new DataChainPointer(dto.getRaw());
    }
}
