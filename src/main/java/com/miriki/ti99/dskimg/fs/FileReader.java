package com.miriki.ti99.dskimg.fs;

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.miriki.ti99.dskimg.api.impl.Ti99ImageServiceImpl;
import com.miriki.ti99.dskimg.domain.DiskFormat;
import com.miriki.ti99.dskimg.domain.FileData;
import com.miriki.ti99.dskimg.domain.FileDescriptorRecord;
import com.miriki.ti99.dskimg.domain.Ti99FileSystem;
import com.miriki.ti99.dskimg.domain.Ti99Image;
import com.miriki.ti99.dskimg.domain.constants.DiskLayoutConstants;
import com.miriki.ti99.dskimg.dto.HfdcDirectoryEntry;
import com.miriki.ti99.dskimg.io.FileDescriptorRecordIO;
import com.miriki.ti99.dskimg.io.Sector;

public final class FileReader {

    private static final Logger log = LoggerFactory.getLogger(FileReader.class);

    private static final int SECTOR_SIZE = DiskLayoutConstants.SECTOR_SIZE;

    private FileReader() {}

    // ============================================================
    //  PUBLIC API – READ FILE
    // ============================================================

    public static FileData readFile(
            Ti99Image image,
            DiskFormat format,
            String fileName) {

        Objects.requireNonNull(image);
        Objects.requireNonNull(format);
        Objects.requireNonNull(fileName);

        log.debug("Reading '{}' from image (format={})", fileName, format);

        // ------------------------------------------------------------
        // 1) Read directory (modern HFDC DTO)
        // ------------------------------------------------------------
        // List<HfdcDirectoryEntry> entries = DirectoryReader.read(image);
        Ti99FileSystem fs = Ti99ImageServiceImpl.loadFileSystem(image);
        List<HfdcDirectoryEntry> entries = DirectoryReader.read(fs);

        HfdcDirectoryEntry entry = entries.stream()
                .filter(e -> e.fileName().equalsIgnoreCase(fileName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("File not found: " + fileName));

        int fdrSector = entry.fdrSector();

        // ------------------------------------------------------------
        // 2) Read FDR
        // ------------------------------------------------------------
        FileDescriptorRecord fdr =
                FileDescriptorRecordIO.readFrom(image.getSector(fdrSector));

        // ------------------------------------------------------------
        // 3) Get HFDC data chain (sector list)
        // ------------------------------------------------------------
        List<Integer> chain = fdr.getDataChain();
        if (chain.isEmpty()) {
            return new FileData(fileName, new byte[0]);
        }

        // ------------------------------------------------------------
        // 4) Read all sectors
        // ------------------------------------------------------------
        byte[] raw = readSectors(image, chain);

        // ------------------------------------------------------------
        // 5) Apply EOF offset
        // ------------------------------------------------------------
        int eof = fdr.getEofOffset();

        if (eof > 0 && eof <= SECTOR_SIZE) {
            int fullSectors = chain.size() - 1;
            int trimmedLength = fullSectors * SECTOR_SIZE + eof;

            byte[] trimmed = new byte[trimmedLength];
            System.arraycopy(raw, 0, trimmed, 0, trimmedLength);

            return new FileData(fileName, trimmed);
        }

        return new FileData(fileName, raw);
    }

    // ============================================================
    //  INTERNAL – READ SECTORS
    // ============================================================

    private static byte[] readSectors(Ti99Image image, List<Integer> chain) {

        byte[] result = new byte[chain.size() * SECTOR_SIZE];

        int offset = 0;
        for (int sectorNumber : chain) {
            Sector sector = image.getSector(sectorNumber);
            if (sector == null) {
                throw new IllegalStateException("Missing sector " + sectorNumber);
            }

            byte[] raw = sector.getBytes();
            System.arraycopy(raw, 0, result, offset, SECTOR_SIZE);
            offset += SECTOR_SIZE;
        }

        return result;
    }
}
