package com.miriki.ti99.imagetools.io;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.miriki.ti99.imagetools.domain.FileDescriptorIndex;

/**
 * IO helper for reading and writing the 256-byte File Descriptor Index (FDI).
 *
 * The FDI is a simple table of 128 entries (2 bytes each), where each entry
 * contains the sector number of a File Descriptor Record (FDR).
 *
 * Layout (256 bytes):
 *   byte 0..1   : FDR #0 (big endian)
 *   byte 2..3   : FDR #1
 *   ...
 *   byte 254..255 : FDR #127
 *
 * This class performs raw sector-level I/O only. Higher-level interpretation
 * is handled by the domain layer.
 */
public final class FileDescriptorIndexIO {

    private static final Logger log = LoggerFactory.getLogger(FileDescriptorIndexIO.class);

    /** Fixed size of the FDI sector */
    private static final int SIZE = 256;

    private FileDescriptorIndexIO() {
        // Utility class – no instances
    }

    // ============================================================
    //  PUBLIC API – READ
    // ============================================================

    /**
     * Reads the File Descriptor Index (FDI) from the given sector.
     *
     * @param sector the sector containing the 256-byte FDI
     * @return a FileDescriptorIndex domain object
     */
    public static FileDescriptorIndex readFrom(Sector sector) {
        Objects.requireNonNull(sector, "Sector must not be null");
        // log.debug("readFrom({})", sector);

        byte[] entries = readSectorBytes(sector);
        return new FileDescriptorIndex(entries);
    }

    // ============================================================
    //  PUBLIC API – WRITE
    // ============================================================

    /**
     * Writes the File Descriptor Index (FDI) into the given sector.
     *
     * @param sector the sector to write into
     * @param fdi    the FileDescriptorIndex domain object
     */
    public static void writeTo(Sector sector, FileDescriptorIndex fdi) {
        Objects.requireNonNull(sector, "Sector must not be null");
        Objects.requireNonNull(fdi, "FileDescriptorIndex must not be null");
        // log.debug("writeTo({}, {})", sector, fdi);

        writeSectorBytes(sector, fdi.getEntries());
    }

    // ============================================================
    //  INTERNAL HELPERS
    // ============================================================

    /**
     * Reads all 256 bytes from the sector into a new array.
     */
    private static byte[] readSectorBytes(Sector sector) {
        byte[] data = new byte[SIZE];
        for (int i = 0; i < SIZE; i++) {
            data[i] = sector.get(i);
        }
        return data;
    }

    /**
     * Writes all 256 bytes into the sector.
     */
    private static void writeSectorBytes(Sector sector, byte[] data) {
        for (int i = 0; i < SIZE; i++) {
            sector.set(i, data[i]);
        }
    }
}
