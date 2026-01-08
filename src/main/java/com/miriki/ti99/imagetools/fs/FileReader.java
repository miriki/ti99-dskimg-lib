package com.miriki.ti99.imagetools.fs;

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.miriki.ti99.imagetools.domain.*;
import com.miriki.ti99.imagetools.io.DirectoryIO;
import com.miriki.ti99.imagetools.io.FileDescriptorRecordIO;
import com.miriki.ti99.imagetools.io.Sector;

/**
 * High-level file reader for TI-99 disk images.
 *
 * Responsibilities:
 *   - locate file via directory
 *   - load File Descriptor Record (FDR)
 *   - follow the data chain (sector list)
 *   - read all sectors into a byte array
 *   - apply EOF offset
 *
 * This class performs *no* interpretation of file formats (PROGRAM, DIS/FIX, etc.).
 * It simply returns the raw file bytes.
 */
public final class FileReader {

    private static final Logger log = LoggerFactory.getLogger(FileReader.class);

    private static final int SECTOR_SIZE = 256;

    private FileReader() {
        // Utility class – no instances
    }

    // ============================================================
    //  PUBLIC API – READ FILE
    // ============================================================

    /**
     * Reads a file by its TI filename (without device prefix).
     *
     * @param image     the disk image
     * @param format    disk format describing geometry and directory layout
     * @param fileName  TI filename (max 10 chars)
     * @return FileData containing name + raw bytes
     */
    public static FileData readFile(
            Ti99Image image,
            DiskFormat format,
            String fileName) {

        log.debug("readFile(image={}, format={}, fileName={})", image, format, fileName);

        Objects.requireNonNull(image, "image must not be null");
        Objects.requireNonNull(format, "format must not be null");
        Objects.requireNonNull(fileName, "fileName must not be null");

        // ------------------------------------------------------------
        // 1) Read directory and locate entry
        // ------------------------------------------------------------
        Directory dir = DirectoryIO.readDirectory(image, format);

        DirectoryEntry entry = dir.findByName(fileName);
        if (entry == null) {
            throw new IllegalArgumentException("File not found: " + fileName);
        }

        int fdrSector = entry.getFdrSector();

        // ------------------------------------------------------------
        // 2) Read FDR
        // ------------------------------------------------------------
        FileDescriptorRecord fdr =
                FileDescriptorRecordIO.readFrom(image.getSector(fdrSector));

        // ------------------------------------------------------------
        // 3) Get data chain (list of sector numbers)
        // ------------------------------------------------------------
        List<Integer> chain = fdr.getDataChain();
        if (chain.isEmpty()) {
            return new FileData(fileName, new byte[0]);
        }

        // ------------------------------------------------------------
        // 4) Read all sectors into a byte array
        // ------------------------------------------------------------
        byte[] raw = readSectors(image, chain);

        // ------------------------------------------------------------
        // 5) Apply EOF offset
        // ------------------------------------------------------------
        int eof = fdr.getEofOffset();

        if (eof > 0 && eof <= raw.length) {
            // eof applies only to the last sector
            int trimmedLength = eof + (chain.size() - 1) * SECTOR_SIZE;
            byte[] trimmed = new byte[trimmedLength];
            System.arraycopy(raw, 0, trimmed, 0, trimmedLength);
            return new FileData(fileName, trimmed);
        }

        // If EOF offset is 0 but file has data, return full raw content
        return new FileData(fileName, raw);
    }

    // ============================================================
    //  INTERNAL – READ SECTORS
    // ============================================================

    /**
     * Reads all sectors listed in the data chain and concatenates them.
     */
    private static byte[] readSectors(Ti99Image image, List<Integer> chain) {

        log.debug("readSectors(image={}, chain={})", image, chain);

        byte[] result = new byte[chain.size() * SECTOR_SIZE];

        int offset = 0;
        for (int sectorNumber : chain) {
            Sector sector = image.getSector(sectorNumber);
            byte[] raw = sector.getBytes();
            System.arraycopy(raw, 0, result, offset, SECTOR_SIZE);
            offset += SECTOR_SIZE;
        }

        return result;
    }
}
