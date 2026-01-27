package com.miriki.ti99.dskimg.api.impl;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.miriki.ti99.dskimg.api.Ti99ImageService;
import com.miriki.ti99.dskimg.domain.*;
import com.miriki.ti99.dskimg.dto.HfdcDirectoryEntry;
import com.miriki.ti99.dskimg.fs.DirectoryReader;
import com.miriki.ti99.dskimg.fs.FileWriter;
import com.miriki.ti99.dskimg.io.FdiDirectoryIO;
import com.miriki.ti99.dskimg.io.FileDescriptorIndexIO;
import com.miriki.ti99.dskimg.io.FileDescriptorRecordIO;
import com.miriki.ti99.dskimg.io.VolumeInformationBlockIO;

/**
 * High-level service implementation for interacting with a TI-99 disk image.
 *
 * Responsibilities:
 *   - import, delete, rename files
 *   - update VIB (Volume Information Block)
 *   - list directory entries
 *
 * This class orchestrates all operations on the disk image.
 * Low-level details (FDR, FDI, ABM, cluster allocation) are delegated
 * to FileWriter and the IO layer.
 */
public final class Ti99ImageServiceImpl implements Ti99ImageService {

    private static final Logger log = LoggerFactory.getLogger(Ti99ImageServiceImpl.class);

    private final Ti99Image image;               // underlying disk image
    private final DiskFormat format;             // geometry + layout
    private VolumeInformationBlock vib;          // cached VIB (kept in sync)

    // ============================================================
    //  CONSTRUCTOR
    // ============================================================

    public Ti99ImageServiceImpl(Ti99Image image) {
        this.image = Objects.requireNonNull(image, "image must not be null");
        this.format = image.getFormat();
        this.vib = VolumeInformationBlockIO.readFrom(image.getSector(format.getVibSector()));
    }

    // ============================================================
    //  IMPORT FILE
    // ============================================================

    @Override
    public void importFile(Ti99File file) throws IOException {
        Objects.requireNonNull(file, "file must not be null");

        log.debug("Importing file '{}' (type={}, recLen={}, flags={})",
                file.getFileName(), file.getType(), file.getRecordLength(), file.getFlags());

        // 1) FIX/VAR packing
        byte[] content = Objects.requireNonNull(file.getContent(), "file content must not be null");

        if (file.isFixed()) {
            file.setPackedContent(FileWriter.buildFixStream(content, file.getRecordLength()));
        } else {
            file.setPackedContent(content);
        }

        // 2) Load filesystem model
        Ti99FileSystem fs = loadFileSystem(image);

        // 3) Write file into image
        vib = FileWriter.createFile(fs, file);
        FdiDirectoryIO dir = FdiDirectoryIO.readFrom(image.getSector(format.getFdiSector()), image);
        List<FdiDirectoryIO.DirectoryEntry> entries = dir.getEntries();
        log.debug("Directory contains {} entries", entries.size());

        // 4) Persist updated VIB
        VolumeInformationBlockIO.writeTo(image.getSector(format.getVibSector()), vib);

        log.info("File '{}' imported successfully", file.getFileName());
    }

    // ============================================================
    //  DELETE FILE (modernized)
    // ============================================================

    @Override
    public void deleteFile(String name) {
        Objects.requireNonNull(name, "name must not be null");

        Ti99FileSystem fs = loadFileSystem(image);
        vib = FileWriter.deleteFile(fs, name);

        VolumeInformationBlockIO.writeTo(image.getSector(format.getVibSector()), vib);

        log.info("File '{}' deleted", name);
    }

    // ============================================================
    //  RENAME FILE (modernized)
    // ============================================================

    @Override
    public void renameFile(String oldName, String newName) {
        Objects.requireNonNull(oldName, "oldName must not be null");
        Objects.requireNonNull(newName, "newName must not be null");

        Ti99FileSystem fs = loadFileSystem(image);
        FileWriter.renameFile(fs, oldName, newName);

        log.info("Renamed '{}' → '{}'", oldName, newName);
    }

    // ============================================================
    //  SET VOLUME NAME
    // ============================================================

    @Override
    public void setVolumeName(String name) {
        Objects.requireNonNull(name, "name must not be null");

        vib.setVolumeName(name);
        VolumeInformationBlockIO.writeTo(image.getSector(format.getVibSector()), vib);

        log.info("Volume name set to '{}'", name);
    }

    // ============================================================
    //  LIST FILES (modernized)
    // ============================================================

    @Override
    public List<HfdcDirectoryEntry> listFiles() {
        Ti99FileSystem fs = loadFileSystem(image);
        return DirectoryReader.read(fs);
    }

    // ============================================================
    //  FILESYSTEM LOADER
    // ============================================================

    public static Ti99FileSystem loadFileSystem(Ti99Image image) {
        Objects.requireNonNull(image);

        DiskFormat format = image.getFormat();

        // Read VIB
        VolumeInformationBlock vib =
                VolumeInformationBlockIO.readFrom(image.getSector(format.getVibSector()));

        // Read ABM
        AllocationBitmap abm = vib.getAbm();

        // Read FDI
        FileDescriptorIndex fdi =
                FileDescriptorIndexIO.readFrom(image.getSector(format.getFdiSector()));

        // Build filesystem model
        Ti99FileSystem fs = new Ti99FileSystem(image);
        fs.setVib(vib);
        fs.setAbm(abm);
        fs.setFdi(fdi);

        // Load FDRs
        for (int sector : fdi.getFdrSectors()) {
            if (sector == 0) continue;

            FileDescriptorRecord fdr =
                    FileDescriptorRecordIO.readFrom(image.getSector(sector));

            if (!fdr.isEmpty() && !fdr.getFileName().isBlank()) {
                fs.addFile(fdr);
            }
        }

        return fs;
    }
}
