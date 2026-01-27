package com.miriki.ti99.dskimg.api;

import java.io.IOException;
import java.util.List;

import com.miriki.ti99.dskimg.domain.Ti99File;
import com.miriki.ti99.dskimg.domain.Ti99Image;
import com.miriki.ti99.dskimg.dto.HfdcDirectoryEntry;

/**
 * High-level service API for interacting with a TI-99 disk image.
 *
 * Responsibilities:
 *   - import, delete, rename files
 *   - update volume metadata
 *   - list directory entries
 *
 * Implementations hide all low-level details (FDR, FDI, ABM, VIB).
 */
public interface Ti99ImageService {

    /**
     * Opens a TI-99 disk image and returns a service wrapper.
     *
     * @param image the disk image
     * @return a Ti99ImageService instance bound to the image
     */
    static Ti99ImageService open(Ti99Image image) {
        return new com.miriki.ti99.dskimg.api.impl.Ti99ImageServiceImpl(image);
    }

    /**
     * Imports a file into the disk image.
     *
     * @param file the file to import
     * @throws IOException if reading the file content fails
     */
    void importFile(Ti99File file) throws IOException;

    /**
     * Deletes a file from the disk image.
     *
     * @param name TI filename (without device prefix)
     */
    void deleteFile(String name);

    /**
     * Renames a file inside the disk image.
     *
     * @param oldName current TI filename
     * @param newName new TI filename
     */
    void renameFile(String oldName, String newName);

    /**
     * Updates the volume name stored in the VIB.
     *
     * @param name new volume name
     */
    void setVolumeName(String name);

    /**
     * Returns a list of directory entries (metadata only).
     *
     * @return list of Ti99DirectoryEntry DTOs
     */
    List<HfdcDirectoryEntry> listFiles();
    
}
