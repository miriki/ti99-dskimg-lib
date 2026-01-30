package com.miriki.ti99.dskimg;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import com.miriki.ti99.dskimg.dto.*;
import com.miriki.ti99.dskimg.domain.DiskFormat;
import com.miriki.ti99.dskimg.domain.DiskFormatPreset;
import com.miriki.ti99.dskimg.domain.enums.FileType;
import com.miriki.ti99.dskimg.domain.enums.RecordFormat;
import com.miriki.ti99.dskimg.domain.fscheck.FilesystemCheckResult;
import com.miriki.ti99.dskimg.domain.fscheck.FilesystemRepairReport;

/**
 * Primary public API for interacting with TI-99/4A disk images.
 *
 * Instances of this interface are obtained through {@link Ti99DskImg}.
 * Implementations are internal and not intended for direct use.
 */
public interface Ti99DiskImage {

    /**
     * Returns the disk format preset used to create or load this image.
     */
    DiskFormatPreset getFormatPreset();

    /**
     * Returns the resolved disk format (geometry, layout, sector count).
     */
    DiskFormat getFormat();

    // ============================================================
    // ================= 1. DISK IMAGE LIFECYCLE ===================
    // ============================================================

    /**
     * Formats the disk image according to the given preset.
     * All sectors are reset and system structures recreated.
     */
    void format(DiskFormatPreset preset);

    void setVolumeName(String name);

    /**
     * Saves the disk image to the specified target path.
     */
    void save(Path target);

    /**
     * Saves the disk image back to its original source path.
     * Throws an exception if the image has no source path.
     */
    void save();

    // ============================================================
    // ==================== 2. FILE OPERATIONS =====================
    // ============================================================

    void writeFile(String name, byte[] data, FileType type, RecordFormat format, int recordLength);
    void writeFile(String name, Path hostFile, FileType type, RecordFormat format, int recordLength) throws IOException;

    void writeTiFile(byte[] data);
    void writeTiFile(Path hostFile) throws IOException;

    byte[] readFile(String name) throws IOException;
    byte[] readTiFile(String name) throws IOException;

    void exportFile(String name, Path target) throws IOException;
    void exportTiFile(String name, Path target) throws IOException;

    void renameFile(String oldName, String newName);

    void deleteFile(String name, boolean safeDelete);

    // ============================================================
    // ===================== 3. DISK ANALYSIS ======================
    // ============================================================

    List<Ti99FileDTO> listFiles();
    
    public boolean exists(String name);

    VolumeInformationBlockDTO getVib();

    AllocationBitmapDTO getAbm();

    List<FileDescriptorRecordDTO> getAllFdrs();

    // ============================================================
    // =============== 4. FILESYSTEM CHECK & REPAIR ================
    // ============================================================

    FilesystemCheckResult check();

    FilesystemRepairReport repair();
}
