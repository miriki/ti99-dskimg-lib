package com.miriki.ti99.imagetools.io;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.miriki.ti99.imagetools.domain.FileDirectoryInformation;

/**
 * IO helper for reading and writing the File Directory Information (FDI) sector.
 *
 * The FDI is a compact table that lists the sector numbers of all
 * File Descriptor Records (FDRs) present on the disk.
 *
 * Layout (256 bytes):
 *   byte 0      : number of FDR entries (N)
 *   byte 1..2   : FDR sector #0 (little endian)
 *   byte 3..4   : FDR sector #1
 *   ...
 *   byte 1+N*2  : last FDR sector
 *   remaining bytes are 0x00
 */
public final class FileDirectoryInformationIO {

    private static final Logger log = LoggerFactory.getLogger(FileDirectoryInformationIO.class);

    private static final int SIZE = 256;

    private FileDirectoryInformationIO() {
        // Utility class – no instances
    }

    // ============================================================
    //  READ FDI FROM SECTOR
    // ============================================================

    /**
     * Reads the File Directory Information (FDI) from the given sector.
     *
     * @param sector the sector containing the FDI
     * @return FileDirectoryInformation domain object
     */
    public static FileDirectoryInformation readFrom(Sector sector) {
        Objects.requireNonNull(sector, "sector must not be null");

        log.debug("readFrom({})", sector);

        byte[] raw = sector.getBytes();

        // Number of FDR entries
        int count = raw[0] & 0xFF;

        List<Integer> fdrSectors = new ArrayList<>(count);

        // Each entry is 2 bytes (little endian)
        for (int i = 0; i < count; i++) {
            int offset = 1 + i * 2;
            int sectorNumber =
                    (raw[offset] & 0xFF) |
                    ((raw[offset + 1] & 0xFF) << 8);

            fdrSectors.add(sectorNumber);
        }

        return new FileDirectoryInformation(fdrSectors);
    }

    // ============================================================
    //  WRITE FDI TO SECTOR
    // ============================================================

    /**
     * Writes the File Directory Information (FDI) into the given sector.
     *
     * @param sector the sector to write into
     * @param fdi    the FileDirectoryInformation domain object
     */
    public static void writeTo(Sector sector, FileDirectoryInformation fdi) {
        Objects.requireNonNull(sector, "sector must not be null");
        Objects.requireNonNull(fdi, "fdi must not be null");

        log.debug("writeTo({}, {})", sector, fdi);

        byte[] raw = new byte[SIZE];

        List<Integer> fdrSectors = fdi.getFdrSectors();
        int count = fdrSectors.size();

        // Write number of entries
        raw[0] = (byte) count;

        // Write each FDR sector number (little endian)
        for (int i = 0; i < count; i++) {
            int sectorNumber = fdrSectors.get(i);
            int offset = 1 + i * 2;

            raw[offset]     = (byte) (sectorNumber & 0xFF);
            raw[offset + 1] = (byte) ((sectorNumber >> 8) & 0xFF);
        }

        // Remaining bytes stay 0x00

        // Write to sector
        for (int i = 0; i < SIZE; i++) {
            sector.set(i, raw[i]);
        }
    }
}
