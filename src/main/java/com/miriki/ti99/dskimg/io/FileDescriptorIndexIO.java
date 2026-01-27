package com.miriki.ti99.dskimg.io;

import java.util.Objects;

import com.miriki.ti99.dskimg.domain.FileDescriptorIndex;
import com.miriki.ti99.dskimg.domain.constants.DiskLayoutConstants;

/**
 * Reads and writes the 256‑byte File Descriptor Index (FDI).
 *
 * The FDI is a table of 128 entries (2 bytes each), where each entry contains
 * the sector number of a File Descriptor Record (FDR).
 */
public final class FileDescriptorIndexIO {

    private FileDescriptorIndexIO() {}

    // ============================================================
    //  PUBLIC API – READ
    // ============================================================

    public static FileDescriptorIndex readFrom(Sector sector) {
        Objects.requireNonNull(sector, "sector must not be null");

        byte[] raw = sector.getBytes(); // defensive copy
        return new FileDescriptorIndex(raw);
    }

    // ============================================================
    //  PUBLIC API – WRITE
    // ============================================================

    public static void writeTo(Sector sector, FileDescriptorIndex fdi) {
        Objects.requireNonNull(sector, "sector must not be null");
        Objects.requireNonNull(fdi, "fdi must not be null");

        byte[] raw = fdi.getEntries();

        if (raw.length != DiskLayoutConstants.SECTOR_SIZE) {
            throw new IllegalArgumentException(
                    "FDI must be exactly " + DiskLayoutConstants.SECTOR_SIZE + " bytes"
            );
        }

        sector.setData(raw);
    }
}
