package com.miriki.ti99.dskimg.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import com.miriki.ti99.dskimg.Ti99DiskImage;
import com.miriki.ti99.dskimg.dto.*;
import com.miriki.ti99.dskimg.fs.FileWriter;
import com.miriki.ti99.dskimg.io.FileDescriptorIndexIO;
import com.miriki.ti99.dskimg.io.FileDescriptorRecordIO;
import com.miriki.ti99.dskimg.io.VolumeInformationBlockIO;
import com.miriki.ti99.dskimg.domain.*;
import com.miriki.ti99.dskimg.domain.enums.FileType;
import com.miriki.ti99.dskimg.domain.enums.RecordFormat;
import com.miriki.ti99.dskimg.domain.fscheck.FilesystemCheckResult;
import com.miriki.ti99.dskimg.domain.fscheck.FilesystemRepairReport;
import com.miriki.ti99.dskimg.domain.io.Ti99FileIO;

public class Ti99DiskImageImpl implements Ti99DiskImage {

    private Path sourcePath;
    private DiskFormatPreset formatPreset;
    private DiskFormat format;
    private byte[] imageData;

    private Ti99Image domainImage;

    @Override
    public DiskFormat getFormat() {
        return format;
    }

    @Override
    public DiskFormatPreset getFormatPreset() {
        return formatPreset;
    }

    public Ti99Image getDomainImage() {
        return domainImage;
    }

    // ============================================================
    // ======================= CONSTRUCTORS ========================
    // ============================================================

    public Ti99DiskImageImpl(Path path) {
        loadFromDisk(path);
    }

    public Ti99DiskImageImpl(DiskFormatPreset preset) {
        createEmptyImage(preset);
    }

    public Ti99DiskImageImpl(Ti99Image image) {
        this.domainImage = Objects.requireNonNull(image);
        this.format = image.getFormat();
        this.formatPreset = null; // kein Preset rekonstruierbar
        this.imageData = image.getRawData();
    }

    // ============================================================
    // ===================== INTERNAL HELPERS ======================
    // ============================================================

    private void loadFromDisk(Path path) {
        try {
            byte[] raw = Files.readAllBytes(path);

            DiskFormatPreset preset = DiskFormat.detectFormat(raw);
            this.formatPreset = preset;
            this.format = preset.getFormat();
            this.domainImage = new Ti99Image(format, raw);
            this.imageData = domainImage.getRawData();
            this.sourcePath = path;

        } catch (IOException e) {
            throw new RuntimeException("Failed to read disk image: " + path, e);
        }
    }

    public static Ti99FileSystem loadFileSystem(Ti99Image image) {
        Objects.requireNonNull(image);

        DiskFormat format = image.getFormat();

        VolumeInformationBlock vib =
                VolumeInformationBlockIO.readFrom(image.getSector(format.getVibSector()));

        AllocationBitmap abm = vib.getAbm();

        FileDescriptorIndex fdi =
                FileDescriptorIndexIO.readFrom(image.getSector(format.getFdiSector()));

        Ti99FileSystem fs = new Ti99FileSystem(image);
        fs.setVib(vib);
        fs.setAbm(abm);
        fs.setFdi(fdi);

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

    private void createEmptyImage(DiskFormatPreset preset) {
        Objects.requireNonNull(preset);

        this.formatPreset = preset;
        this.format = preset.getFormat();
        this.domainImage = new Ti99Image(format);
        this.imageData = domainImage.getRawData();
        this.sourcePath = null;
    }

    private void flushDomainToBytes() {
        // domainImage.toBytes() – falls später benötigt
    }

    // ============================================================
    // =============== 1. DISK IMAGE LIFECYCLE =====================
    // ============================================================

    @Override
    public void format(DiskFormatPreset preset) {
        createEmptyImage(preset);
    }

    @Override
    public void setVolumeName(String name) {
        Objects.requireNonNull(name);

        Ti99FileSystem fs = loadFileSystem(domainImage);
        fs.getVib().setVolumeName(name);

        VolumeInformationBlockIO.writeTo(
                domainImage.getSector(format.getVibSector()),
                fs.getVib()
        );
    }

    @Override
    public void save(Path target) {
        Objects.requireNonNull(target);

        flushDomainToBytes();

        try {
            Files.write(target, imageData);
            this.sourcePath = target;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save disk image: " + target, e);
        }
    }

    @Override
    public void save() {
        if (sourcePath == null) {
            throw new IllegalStateException("No source path defined. Use save(Path) instead.");
        }
        save(sourcePath);
    }

    // ============================================================
    // ================== 2. FILE OPERATIONS =======================
    // ============================================================

    @Override
    public void writeFile(String name, byte[] data, FileType type, RecordFormat format, int recordLength) {

        Objects.requireNonNull(name);
        Objects.requireNonNull(data);
        Objects.requireNonNull(type);
        Objects.requireNonNull(format);

        Ti99FileSystem fs = loadFileSystem(domainImage);

        Ti99File file = new Ti99File();
        file.setFileName(name);
        file.setContent(data);
        file.setType(type);
        file.setFormat(format);
        file.setRecordLength(recordLength);

        FileWriter.createFile(fs, file);
    }

    @Override
    public void writeFile(String name, Path hostFile, FileType type, RecordFormat format, int recordLength) throws IOException {
        byte[] data = Files.readAllBytes(hostFile);
        writeFile(name, data, type, format, recordLength);
    }

    @Override
    public void writeTiFile(byte[] tifilesData) {
        throw new UnsupportedOperationException(
                "TI-FILES import is handled by the FIAD module. " +
                "Use FiadService.importTiFile(...) instead."
        );
    }

    @Override
    public void writeTiFile(Path hostFile) {
        throw new UnsupportedOperationException(
                "TI-FILES import is handled by the FIAD module. " +
                "Use FiadService.importTiFile(...) instead."
        );
    }

    @Override
    public byte[] readFile(String name) throws IOException {

        Objects.requireNonNull(name);

        Ti99FileSystem fs = loadFileSystem(domainImage);

        FileDescriptorRecord fdr = fs.getFiles().stream()
                .filter(f -> name.equalsIgnoreCase(f.getFileName()))
                .findFirst()
                .orElse(null);

        if (fdr == null) {
            throw new IOException("File not found: " + name);
        }

        Ti99File file = Ti99FileIO.readFile(fs, fdr);

        return file.getContent();
    }

    @Override
    public byte[] readTiFile(String name) throws IOException {

        Ti99FileSystem fs = loadFileSystem(domainImage);

        FileDescriptorRecord fdr = fs.getFiles().stream()
                .filter(f -> name.equalsIgnoreCase(f.getFileName()))
                .findFirst()
                .orElseThrow(() -> new IOException("File not found: " + name));

        Ti99File file = Ti99FileIO.readFile(fs, fdr);

        // Die Library liefert nur Content.
        // FIAD erzeugt den TI-FILES-Header.
        return file.getContent();
    }

    @Override
    public void exportFile(String name, Path target) throws IOException {
        byte[] data = readFile(name);
        Files.write(target, data);
    }

    @Override
    public void exportTiFile(String name, Path target) throws IOException {
        byte[] data = readTiFile(name);
        Files.write(target, data);
    }

    @Override
    public void renameFile(String oldName, String newName) {
        Objects.requireNonNull(oldName);
        Objects.requireNonNull(newName);

        Ti99FileSystem fs = loadFileSystem(domainImage);
        FileWriter.renameFile(fs, oldName, newName);
    }

    @Override
    public void deleteFile(String name, boolean safeDelete) {
        Objects.requireNonNull(name);

        Ti99FileSystem fs = loadFileSystem(domainImage);
        FileWriter.deleteFile(fs, name);
    }

    // ============================================================
    // ==================== 3. DISK ANALYSIS ========================
    // ============================================================

    @Override
    public List<Ti99FileDTO> listFiles() {

        Ti99FileSystem fs = loadFileSystem(domainImage);

        return Ti99FileIO.readDirectory(fs).stream()
                .map(Ti99FileMapper::toDTO)
                .toList();
    }

    @Override
    public boolean exists(String name) {
        Objects.requireNonNull(name);
        return listFiles().stream()
                .anyMatch(f -> name.equalsIgnoreCase(f.getFileName()));
    }

    @Override
    public VolumeInformationBlockDTO getVib() { return null; }

    @Override
    public AllocationBitmapDTO getAbm() { return null; }

    @Override
    public List<FileDescriptorRecordDTO> getAllFdrs() { return List.of(); }

    // ============================================================
    // =========== 4. FILESYSTEM CHECK & REPAIR ====================
    // ============================================================

    @Override
    public FilesystemCheckResult check() { return null; }

    @Override
    public FilesystemRepairReport repair() { return null; }
}
