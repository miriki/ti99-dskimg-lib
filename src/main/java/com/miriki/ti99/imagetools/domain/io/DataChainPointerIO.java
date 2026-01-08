package com.miriki.ti99.imagetools.domain.io;

import java.util.Objects;

import com.miriki.ti99.imagetools.domain.DataChainPointer;
import com.miriki.ti99.imagetools.io.Sector;

/**
 * IO helper for reading and writing HFDC Data Chain Pointer (DCP) entries.
 *
 * A DCP entry is always 3 bytes:
 *   - byte 0: high byte of start sector
 *   - byte 1: low byte of start sector
 *   - byte 2: (length-1)<<4  (upper nibble), lower nibble unused
 *
 * This class performs only raw byte transfer; interpretation happens in DataChainPointer.
 */
public final class DataChainPointerIO {

    private static final int ENTRY_SIZE = 3;

    private DataChainPointerIO() {
        // Utility class – no instances
    }

    // ============================================================
    //  PUBLIC API – READ
    // ============================================================

    /**
     * Reads a 3‑byte Data Chain Pointer from a sector at the given offset.
     */
    public static DataChainPointer read(Sector sector, int offset) {
        Objects.requireNonNull(sector, "Sector must not be null");

        byte[] raw = new byte[ENTRY_SIZE];
        for (int i = 0; i < ENTRY_SIZE; i++) {
            raw[i] = sector.get(offset + i);
        }

        return new DataChainPointer(raw);
    }

    // ============================================================
    //  PUBLIC API – WRITE
    // ============================================================

    /**
     * Writes a 3‑byte Data Chain Pointer into a sector at the given offset.
     */
    public static void write(Sector sector, int offset, DataChainPointer dcp) {
        Objects.requireNonNull(sector, "Sector must not be null");
        Objects.requireNonNull(dcp, "DataChainPointer must not be null");

        byte[] raw = dcp.getRaw();
        for (int i = 0; i < ENTRY_SIZE; i++) {
            sector.set(offset + i, raw[i]);
        }
    }
}
