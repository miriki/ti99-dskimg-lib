package com.miriki.ti99.dskimg.fs;

import java.io.IOException;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.miriki.ti99.dskimg.impl.Ti99DiskImageImpl;
import com.miriki.ti99.dskimg.domain.*;

/**
 * High-level file import/export operations for TI-99 disk images.
 *
 * Responsibilities:
 *   - import a Ti99File into an image (delegates to FileWriter)
 *   - delete a file (delegates to FileWriter)
 *   - rename a file (delegates to FileWriter)
 *
 * This class contains no filesystem logic.
 */
public final class FileImporter {

    private static final Logger log = LoggerFactory.getLogger(FileImporter.class);

    private FileImporter() {}

    // ============================================================
    //  IMPORT FILE
    // ============================================================

    public static void importFile(Ti99Image image, Ti99File file) throws IOException { 
        throw new UnsupportedOperationException("Use Ti99ImageService.importFile() instead of FileImporter.importFile()");
        /*
        
    	Objects.requireNonNull(image); 
    	Objects.requireNonNull(file);

        DiskFormat format = image.getFormat();

        log.debug("Importing '{}' into image (format={})", file.getFileName(), image.getFormat());

        // 1) Read VIB
        VolumeInformationBlock vib =
                VolumeInformationBlockIO.readFrom(image.getSector(format.getVibSector()));

        // 2) Prepare packed content (FIX files need sector-aligned packing)
        byte[] content = file.getContent();
        byte[] packed = (file.getRecordLength() > 0 && !file.isVariable())
                ? buildFixStream(content, file.getRecordLength())
                : content;

        file.setPackedContent(packed);

        // 3) Delegate to FileWriter
        // VolumeInformationBlock updatedVib = FileWriter.createFile(image, format, vib, file);
        Ti99FileSystem fs = loadFileSystem(image);
        VolumeInformationBlock updatedVib = FileWriter.createFile(fs, file);

        // 4) Write updated VIB
        VolumeInformationBlockIO.writeTo(image.getSector(format.getVibSector()),updatedVib);

        log.trace("File '{}' imported successfully", file.getFileName());
        */
    }

    // ============================================================
    //  DELETE FILE
    // ============================================================

    public static void deleteFile(Ti99Image image, String fileName) {
        Objects.requireNonNull(image);
        Objects.requireNonNull(fileName);

        // DiskFormat format = image.getFormat();

        log.debug("Deleting '{}' on image (format={})", fileName, image.getFormat());

        // VolumeInformationBlock vib = VolumeInformationBlockIO.readFrom(image.getSector(format.getVibSector()));

        // VolumeInformationBlock updatedVib = FileWriter.deleteFile(image, format, vib, fileName);
        Ti99FileSystem fs = Ti99DiskImageImpl.loadFileSystem(image); 
        FileWriter.deleteFile(fs, fileName);

        // VolumeInformationBlockIO.writeTo( image.getSector(format.getVibSector()), updatedVib );

        log.info("File '{}' deleted", fileName);
    }

    // ============================================================
    //  RENAME FILE
    // ============================================================

    public static void renameFile(Ti99Image image, String oldName, String newName) {
        Objects.requireNonNull(image);
        Objects.requireNonNull(oldName);
        Objects.requireNonNull(newName);

        log.debug("Renaming '{}' --> '{}' on image", oldName, newName);

        // Neues Filesystem laden
        Ti99FileSystem fs = Ti99DiskImageImpl.loadFileSystem(image);

        // Modernisierte API aufrufen
        FileWriter.renameFile(fs, oldName, newName);

        log.info("File renamed");
    }
}
