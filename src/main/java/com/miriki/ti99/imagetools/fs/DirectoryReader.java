package com.miriki.ti99.imagetools.fs;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.miriki.ti99.imagetools.domain.*;
import com.miriki.ti99.imagetools.dto.Ti99DirectoryEntry;
import com.miriki.ti99.imagetools.fs.format.FileTypeFormatter;
import com.miriki.ti99.imagetools.fs.format.RecordLengthFormatter;
import com.miriki.ti99.imagetools.io.FileDescriptorRecordIO;
import com.miriki.ti99.imagetools.io.Sector;

/**
 * High-level directory reader for TI-99 disk images.
 *
 * Responsibilities:
 *   - read FDI (File Directory Information)
 *   - load each referenced File Descriptor Record (FDR)
 *   - extract metadata (file type, record format, timestamps, etc.)
 *   - convert everything into Ti99DirectoryEntry DTOs
 *
 * This class does not interpret file contents; it only reads metadata.
 */
public final class DirectoryReader {

    private DirectoryReader() {
        // Utility class – no instances
    }

    // ============================================================
    //  PUBLIC API – READ DIRECTORY
    // ============================================================

    /**
     * Reads the directory of the disk image and returns a list of DTO entries.
     *
     * @param image the TI-99 disk image
     * @return list of Ti99DirectoryEntry objects
     */
    public static List<Ti99DirectoryEntry> read(Ti99Image image) {
        Objects.requireNonNull(image, "image must not be null");

        DiskFormat format = image.getFormat();
        Sector fdiSector = image.getSector(format.getFdiSector());

        // ------------------------------------------------------------
        // 1) Read FDI (list of FDR sector numbers)
        // ------------------------------------------------------------
        FdiDirectory fdi = FdiDirectory.readFrom(fdiSector);

        List<Ti99DirectoryEntry> result = new ArrayList<>();

        // ------------------------------------------------------------
        // 2) For each FDR sector, load metadata and build DTO
        // ------------------------------------------------------------
        for (FdiDirectory.DirectoryEntry e : fdi.getEntries()) {

            // Load FDR
            FileDescriptorRecord fdr =
                    FileDescriptorRecordIO.readFrom(image.getSector(e.fdrSector()));

            FileType fileType = FileType.fromCode(fdr.getFileType());

            // Build DTO
            Ti99DirectoryEntry dto = new Ti99DirectoryEntry(
                    fdr.getFileName(),
                    fdr.getTotalSectorsAllocated(),
                    fileType.name(),                         // raw type code
                    FileTypeFormatter.describe(fdr),         // formatted type
                    RecordFormat.fromFdr(fdr).description(), // "Fixed" / "Variable"
                    RecordLengthFormatter.describe(fdr),     // "80 bytes"
                    fdr.getFileSizeInBytes(),
                    fdr.isProtected(),
                    fdr.isFragmented(),
                    fdr.getCreationTimestamp(),
                    fdr.getUpdateTimestamp()
            );

            result.add(dto);
        }

        return result;
    }
}
