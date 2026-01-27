package com.miriki.ti99.dskimg.domain;

import java.util.Arrays;
import java.util.Objects;

import com.miriki.ti99.dskimg.domain.constants.FdrConstants;
import com.miriki.ti99.dskimg.dto.DataChainPointerDTO;

/**
 * Represents a single HFDC Data Chain Pointer (DCP) entry.
 *
 * A DCP entry is exactly 3 bytes and encodes:
 *
 *   - n0 = start sector (12 bits)
 *   - m0 = sector count (12 bits)
 *
 * TI-DOS quirk:
 *   If m0 == 0, this means "1 cluster".
 */
public final class DataChainPointer {

    /** Raw 3-byte DCP entry. */
    private final byte[] raw;

    // ============================================================
    //  CONSTRUCTOR
    // ============================================================

    /**
     * Creates a DataChainPointer from a raw 3-byte array.
     */
    public DataChainPointer(byte[] raw) {
        Objects.requireNonNull(raw, "raw must not be null");
        if (raw.length != FdrConstants.DCP_ENTRY_SIZE) {
            throw new IllegalArgumentException("Data chain pointer must be exactly 3 bytes");
        }
        this.raw = Arrays.copyOf(raw, raw.length);
    }

    // ============================================================
    //  BASIC ACCESSOR
    // ============================================================

    /** Returns a defensive copy of the raw 3-byte DCP entry. */
    public byte[] getRaw() {
        return Arrays.copyOf(raw, raw.length);
    }

    // ============================================================
    //  DECODED FIELDS
    // ============================================================

    /** Returns the first sector (n0). */
    public int getFirstSector() {
        int n2n1 = raw[0] & 0xFF;
        int m1n3 = raw[1] & 0xFF;
        return ((m1n3 & 0x0F) << 8) | n2n1;
    }

    /** Returns the sector count (m0). */
    public int getSectorCount() {
        int m1n3 = raw[1] & 0xFF;
        int m3m2 = raw[2] & 0xFF;
        return ((m3m2 << 8) | (m1n3 & 0xF0)) >> 4;
    }

    /**
     * Returns the last sector (n0 + m0 - 1).
     * TI-DOS quirk: m0 == 0 → treat as 1 cluster.
     */
    public int getLastSector() {
        int first = getFirstSector();
        int count = getSectorCount();
        return (count == 0) ? first : first + count - 1;
    }

    // ============================================================
    //  SEMANTIC HELPERS
    // ============================================================

    /** True if both start sector and count are zero. */
    public boolean isEmpty() {
        return getFirstSector() == 0 && getSectorCount() == 0;
    }

    /** True if TI-DOS would interpret this as a single cluster. */
    public boolean isSingleCluster() {
        return getSectorCount() == 0;
    }

    /** Returns the number of clusters (TI-DOS quirk aware). */
    public int clusterCount() {
        int count = getSectorCount();
        return (count == 0) ? 1 : count;
    }

    // ============================================================
    //  DTO CONVERSION
    // ============================================================

    public DataChainPointerDTO toDTO() {
        return new DataChainPointerDTO(raw);
    }

    public static DataChainPointer fromDTO(DataChainPointerDTO dto) {
        return new DataChainPointer(dto.getRaw());
    }

    // ============================================================
    //  DEBUG STRING
    // ============================================================

    @Override
    public String toString() {
        return "DCP{" +
                "first=" + getFirstSector() +
                ", count=" + getSectorCount() +
                ", last=" + getLastSector() +
                '}';
    }
}
