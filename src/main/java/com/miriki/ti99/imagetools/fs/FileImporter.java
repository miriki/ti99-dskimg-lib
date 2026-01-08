package com.miriki.ti99.imagetools.fs;

import java.io.IOException;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.miriki.ti99.imagetools.domain.*;
import com.miriki.ti99.imagetools.io.*;

/**
 * High-level file import/export operations for TI-99 disk images.
 *
 * Responsibilities:
 *   - import a Ti99File into an image (create file, update ABM + VIB)
 *   - delete a file (free sectors, update FDI + ABM + VIB)
 *   - rename a file (update FDR + FDI)
 *
 * This class delegates all heavy lifting to FileWriter and the IO layer.
 */
public final class FileImporter {

    private static final Logger log = LoggerFactory.getLogger(FileImporter.class);

    private FileImporter() {
        // Utility class – no instances
    }

    // ============================================================
    //  IMPORT FILE
    // ============================================================

    /**
     * Imports a Ti99File into the disk image.
     *
     * Steps:
     *   1) Read VIB
     *   2) Create file via FileWriter (allocates sectors, writes FDR, updates FDI)
     *   3) Write updated VIB back to disk
     */
    public static void importFile(Ti99Image image, Ti99File file) throws IOException {
        log.info("importFile(image={}, file={})", image, file);

        Objects.requireNonNull(image, "image must not be null");
        Objects.requireNonNull(file, "file must not be null");

        DiskFormat format = image.getFormat();

        // Read VIB
        VolumeInformationBlock vib =
                VolumeInformationBlockIO.readFrom(image.getSector(format.getVibSector()));

        // Create file (updates ABM + FDI)
        FileWriter.createFile(
                image,
                format,
                vib,
                file.getFileName(),
                file.getContent()
        );

        log.info("  file imported to image");
    }

    // ============================================================
    //  DELETE FILE
    // ============================================================

    /**
     * Deletes a file from the disk image.
     *
     * Steps:
     *   1) Read VIB
     *   2) Delete file via FileWriter (free sectors, update FDI)
     *   3) Write updated VIB back to disk
     */
    public static void deleteFile(Ti99Image image, String fileName) {
        log.info("deleteFile(image={}, fileName={})", image, fileName);

        Objects.requireNonNull(image, "image must not be null");
        Objects.requireNonNull(fileName, "fileName must not be null");

        DiskFormat format = image.getFormat();

        // Read VIB
        VolumeInformationBlock vib =
                VolumeInformationBlockIO.readFrom(image.getSector(format.getVibSector()));

        // Delete file (updates ABM + FDI)
        VolumeInformationBlock updatedVib =
                FileWriter.deleteFile(image, format, vib, fileName);

        // Write updated VIB
        VolumeInformationBlockIO.writeTo(
                image.getSector(format.getVibSector()),
                updatedVib
        );

        log.info("  file deleted from image");
    }

    // ============================================================
    //  RENAME FILE
    // ============================================================

    /**
     * Renames a file inside the disk image.
     *
     * Note:
     *   Renaming does not change ABM or VIB.
     *   Only FDR + FDI are updated.
     */
    public static void renameFile(Ti99Image image, String oldName, String newName) {
        log.info("renameFile(image={}, oldName={}, newName={})",
                image, oldName, newName);

        Objects.requireNonNull(image, "image must not be null");
        Objects.requireNonNull(oldName, "oldName must not be null");
        Objects.requireNonNull(newName, "newName must not be null");

        DiskFormat format = image.getFormat();

        // Rename file (updates FDR + FDI)
        FileWriter.renameFile(image, format, oldName, newName);

        log.info("  file renamed in image");
    }
}
