package com.miriki.ti99.dskimg.io;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.miriki.ti99.dskimg.domain.FileDirectoryInformation;
import com.miriki.ti99.dskimg.domain.constants.DiskLayoutConstants;

/**
 * Reads and writes the File Directory Information (FDI) sector.
 *
 * Layout (256 bytes):
 *   byte 0      : number of FDR entries (N)
 *   byte 1..2   : FDR sector #0 (little endian)
 *   byte 3..4   : FDR sector #1
 *   ...
 */
public final class FileDirectoryInformationIO {

    private FileDirectoryInformationIO() {}

    private static final int COUNT_OFFSET = 0;
    private static final int FIRST_ENTRY_OFFSET = 1;
    private static final int ENTRY_SIZE = 2;

    // ============================================================
    //  READ FDI FROM SECTOR
    // ============================================================

    public static FileDirectoryInformation readFrom(Sector sector) {
        Objects.requireNonNull(sector, "sector must not be null");

        byte[] raw = sector.getBytes();

        int count = raw[COUNT_OFFSET] & 0xFF;

        List<Integer> fdrSectors = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            int offset = FIRST_ENTRY_OFFSET + i * ENTRY_SIZE;

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

    public static void writeTo(Sector sector, FileDirectoryInformation fdi) {
        Objects.requireNonNull(sector, "sector must not be null");
        Objects.requireNonNull(fdi, "fdi must not be null");

        byte[] raw = new byte[DiskLayoutConstants.SECTOR_SIZE];

        List<Integer> fdrSectors = fdi.getFdrSectors();
        int count = fdrSectors.size();

        raw[COUNT_OFFSET] = (byte) count;

        for (int i = 0; i < count; i++) {
            int sectorNumber = fdrSectors.get(i);
            int offset = FIRST_ENTRY_OFFSET + i * ENTRY_SIZE;

            raw[offset]     = (byte) (sectorNumber & 0xFF);
            raw[offset + 1] = (byte) ((sectorNumber >> 8) & 0xFF);
        }

        sector.setData(raw);
    }
}
