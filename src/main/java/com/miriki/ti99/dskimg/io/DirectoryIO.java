package com.miriki.ti99.dskimg.io;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.miriki.ti99.dskimg.domain.*;
import com.miriki.ti99.dskimg.domain.constants.DiskLayoutConstants;

/**
 * Reads and writes the high-level directory of a TI‑99 disk image.
 *
 * The directory is constructed from:
 *   - the File Descriptor Index (FDI)
 *   - all referenced File Descriptor Records (FDRs)
 */
public final class DirectoryIO {

    private DirectoryIO() {}

    // ============================================================
    //  READ DIRECTORY
    // ============================================================

    /**
     * Reads the directory of the given TI‑99 image using the provided disk format.
     */
    public static Directory readDirectory(Ti99Image image, DiskFormat format) {
        Objects.requireNonNull(image, "image must not be null");
        Objects.requireNonNull(format, "format must not be null");

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
                continue; // unused FDI entry
            }

            // Validate sector index
            if (fdrSectorNumber < 0 || fdrSectorNumber >= image.getTotalSectors()) {
                continue; // skip corrupt entries
            }

            Sector fdrSector = image.getSector(fdrSectorNumber);
            FileDescriptorRecord fdr = FileDescriptorRecordIO.readFrom(fdrSector);

            String name = fdr.getFileName();

            // Skip empty or blank names
            if (name == null || name.isBlank()) {
                continue;
            }

            entries.add(new DirectoryEntry(name.trim(), fdrSectorNumber));
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
     * This is the TI‑99 equivalent of an empty directory.
     */
    public static void writeEmptyDirectory(Ti99Image image, DiskFormat format) {
        Objects.requireNonNull(image, "image must not be null");
        Objects.requireNonNull(format, "format must not be null");

        int first = format.getFirstFdrSector();
        int count = format.getFdrSectorCount();

        byte[] empty = new byte[DiskLayoutConstants.SECTOR_SIZE];

        for (int i = 0; i < count; i++) {
            image.getSector(first + i).setData(empty);
        }
    }
}
