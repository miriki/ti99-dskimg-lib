package com.miriki.ti99.imagetools.io;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.miriki.ti99.imagetools.domain.*;

/**
 * IO helper for reading the high-level directory of a TI-99 disk image.
 *
 * The directory is constructed from:
 *   - the File Descriptor Index (FDI)
 *   - all referenced File Descriptor Records (FDRs)
 */
public final class DirectoryIO {

    private static final Logger log = LoggerFactory.getLogger(DirectoryIO.class);

    private DirectoryIO() {
        // Utility class – no instances
    }

    // ============================================================
    //  READ DIRECTORY
    // ============================================================

    /**
     * Reads the directory of the given TI-99 image using the provided disk format.
     *
     * Steps:
     *   1) Read the FDI sector (list of FDR sector numbers)
     *   2) For each FDR sector:
     *        - read the File Descriptor Record
     *        - extract the file name
     *        - create a DirectoryEntry
     */
    public static Directory readDirectory(Ti99Image image, DiskFormat format) {
        Objects.requireNonNull(image, "image must not be null");
        Objects.requireNonNull(format, "format must not be null");

        log.debug("readDirectory(image={}, format={})", image, format);

        // ------------------------------------------------------------
        // 1) Read FDI (File Descriptor Index)
        // ------------------------------------------------------------
        Sector fdiSector = image.getSector(format.getFdiSector());
        FileDescriptorIndex fdi = FileDescriptorIndexIO.readFrom(fdiSector);

        List<DirectoryEntry> entries = new ArrayList<>();

        // ------------------------------------------------------------
        // 2) Iterate over all FDR sectors listed in the FDI
        // ------------------------------------------------------------
        for (int fdrSectorNumber : fdi.getFdrSectors()) {

            if (fdrSectorNumber == 0) {
                // unused FDI entry
                continue;
            }

            Sector fdrSector = image.getSector(fdrSectorNumber);
            FileDescriptorRecord fdr = FileDescriptorRecordIO.readFrom(fdrSector);

            String name = fdr.getFileName().trim();

            // Only add valid entries (empty names = unused FDR slots)
            if (!name.isEmpty()) {
                entries.add(new DirectoryEntry(name, fdrSectorNumber));
            }
        }

        // ------------------------------------------------------------
        // 3) Return directory
        // ------------------------------------------------------------
        return new Directory(entries);
    }

    // ============================================================
    //  WRITE EMPTY DIRECTORY (FDR ZONE)
    // ============================================================

    /**
     * Clears all FDR sectors (File Descriptor Records).
     * This is the TI-99 equivalent of an empty directory.
     */
    public static void writeEmptyDirectory(Ti99Image image, DiskFormat format) {

        int first = format.getFirstFdrSector();
        int count = format.getFdrSectorCount();

        byte[] empty = new byte[Sector.SIZE];

        for (int i = 0; i < count; i++) {
            Sector sector = image.getSector(first + i);
            sector.setData(empty);
        }
    }
}
