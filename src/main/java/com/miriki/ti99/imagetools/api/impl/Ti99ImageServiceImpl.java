package com.miriki.ti99.imagetools.api.impl;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import com.miriki.ti99.imagetools.api.Ti99ImageService;
import com.miriki.ti99.imagetools.domain.*;
import com.miriki.ti99.imagetools.dto.Ti99DirectoryEntry;
import com.miriki.ti99.imagetools.fs.DirectoryReader;
import com.miriki.ti99.imagetools.fs.FileWriter;
import com.miriki.ti99.imagetools.io.VolumeInformationBlockIO;

/**
 * High-level service implementation for interacting with a TI-99 disk image.
 *
 * Responsibilities:
 *   - import, delete, rename files
 *   - update VIB (Volume Information Block)
 *   - list directory entries
 *
 * This class delegates all low-level work to FileWriter, DirectoryReader,
 * and the IO layer. It maintains an in-memory VIB instance that is kept
 * in sync with the image.
 */
public final class Ti99ImageServiceImpl implements Ti99ImageService {

    private final Ti99Image image;               // underlying disk image
    private final DiskFormat format;             // geometry + layout
    private VolumeInformationBlock vib;          // cached VIB (kept in sync)

    // ============================================================
    //  CONSTRUCTOR
    // ============================================================

    /**
     * Creates a service wrapper around a TI-99 disk image.
     * Loads the VIB immediately.
     */
    public Ti99ImageServiceImpl(Ti99Image image) {
        this.image = Objects.requireNonNull(image, "image must not be null");
        this.format = image.getFormat();
        this.vib = VolumeInformationBlockIO.readFrom(image.getSector(format.getVibSector()));
    }

    // ============================================================
    //  IMPORT FILE
    // ============================================================

    /**
     * Imports a file into the disk image.
     * Updates the VIB and writes it back to disk.
     */
    @Override
    public void importFile(Ti99File file) throws IOException {
        Objects.requireNonNull(file, "file must not be null");

        vib = FileWriter.createFile(
                image,
                format,
                vib,
                // file.getFileName(),
                // file.getContent()
                file
        );

        VolumeInformationBlockIO.writeTo(image.getSector(format.getVibSector()), vib);
    }

    // ============================================================
    //  DELETE FILE
    // ============================================================

    /**
     * Deletes a file from the disk image.
     * Updates the VIB and writes it back to disk.
     */
    @Override
    public void deleteFile(String name) {
        Objects.requireNonNull(name, "name must not be null");

        vib = FileWriter.deleteFile(image, format, vib, name);
        VolumeInformationBlockIO.writeTo(image.getSector(format.getVibSector()), vib);
    }

    // ============================================================
    //  RENAME FILE
    // ============================================================

    /**
     * Renames a file inside the disk image.
     * Does not modify the VIB (ABM unchanged).
     */
    @Override
    public void renameFile(String oldName, String newName) {
        Objects.requireNonNull(oldName, "oldName must not be null");
        Objects.requireNonNull(newName, "newName must not be null");

        FileWriter.renameFile(image, format, oldName, newName);
    }

    // ============================================================
    //  SET VOLUME NAME
    // ============================================================

    /**
     * Updates the volume name in the VIB and writes it back to disk.
     */
    @Override
    public void setVolumeName(String name) {
        Objects.requireNonNull(name, "name must not be null");

        vib.setVolumeName(name);
        VolumeInformationBlockIO.writeTo(image.getSector(format.getVibSector()), vib);
    }

    // ============================================================
    //  LIST FILES
    // ============================================================

    /**
     * Returns a list of directory entries (DTOs).
     */
    @Override
    public List<Ti99DirectoryEntry> listFiles() {
        return DirectoryReader.read(image);
    }
}
