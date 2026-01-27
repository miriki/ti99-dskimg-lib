package com.miriki.ti99.dskimg.fs;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import com.miriki.ti99.dskimg.domain.*;
import com.miriki.ti99.dskimg.domain.enums.FileType;
import com.miriki.ti99.dskimg.domain.enums.RecordFormat;
import com.miriki.ti99.dskimg.dto.HfdcDirectoryEntry;
import com.miriki.ti99.dskimg.fs.format.RecordFormatFormatter;
import com.miriki.ti99.dskimg.fs.format.RecordLengthFormatter;

/**
 * High-level directory reader that converts the in-memory filesystem model
 * (Ti99FileSystem) into a list of HfdcDirectoryEntry DTOs.
 *
 * This version performs no IO. All data comes from the filesystem model.
 */
public final class DirectoryReader {

    private DirectoryReader() {}

    /**
     * Converts the filesystem's FDR list into directory entries.
     */
    public static List<HfdcDirectoryEntry> read(Ti99FileSystem fs) {
        Objects.requireNonNull(fs, "filesystem must not be null");
        fs.validate();

        List<HfdcDirectoryEntry> result = new ArrayList<>();

        for (FileDescriptorRecord fdr : fs.getFiles()) {

            FdrStatus status = fdr.getStatus();
            FileType fileType = status.getType();
            RecordFormat recordFormat = status.getFormat();
            RecordLength recordLength = RecordLength.fromFdr(fdr);

            String typeDescription =
                    fileType.formatLabel(recordFormat, recordLength.length());

            String formatDescription =
                    RecordFormatFormatter.describe(recordFormat);

            String lengthDescription =
                    RecordLengthFormatter.describe(recordLength);

            result.add(new HfdcDirectoryEntry(
                    fdr.getFileName(),
                    fs.getSectorForFile(fdr.getFileName()),
                    fdr.getTotalSectorsAllocated(),
                    fdr.getFileSizeInBytes(),
                    typeDescription,
                    formatDescription,
                    lengthDescription,
                    status.getAttributes().contains(FileAttribute.PROTECTED),
                    fdr.isFragmented(),
                    fdr.getCreationTimestamp(),
                    fdr.getUpdateTimestamp()
            ));
        }

        // Alphabetisch sortieren
        result.sort(Comparator.comparing(HfdcDirectoryEntry::fileName,
                String.CASE_INSENSITIVE_ORDER));

        return result;
    }
}
