package com.miriki.ti99.imagetools.dto;

import java.util.Arrays;
import java.util.Objects;

/**
 * Immutable DTO representing a fully extracted TI-99 file.
 *
 * Contains:
 *   - metadata (name, type, flags, timestamps)
 *   - record structure (record length, records per sector)
 *   - raw file content (already de-chained and linearized)
 *   - computed size in bytes
 *
 * This DTO is used by UI, exporters, editors, and the domain layer.
 */
public final class Ti99FileDTO {

    private final String fileName;        // normalized TI filename
    private final String fileType;        // "PROGRAM", "DIS/FIX", "DIS/VAR", ...
    private final int recordLength;       // 0 for PROGRAM files
    private final byte[] content;         // full linearized file content

    private final int flags;              // raw FDR status byte (type + flags)
    private final int recordsPerSector;   // derived from format + record length
    private final int size;               // file size in bytes
    private final long timestamp;         // best timestamp (created/updated)

    // ============================================================
    //  CONSTRUCTOR
    // ============================================================

    public Ti99FileDTO(
            String fileName,
            String fileType,
            int recordLength,
            byte[] content,
            int flags,
            int recordsPerSector,
            int size,
            long timestamp
    ) {
        Objects.requireNonNull(fileName, "Filename must not be null");
        Objects.requireNonNull(fileType, "File type must not be null");
        Objects.requireNonNull(content, "Content must not be null");

        if (fileName.isBlank()) {
            throw new IllegalArgumentException("Filename must not be empty");
        }
        if (fileType.isBlank()) {
            throw new IllegalArgumentException("File type must not be empty");
        }
        if (recordLength < 0) {
            throw new IllegalArgumentException("Record length must be >= 0");
        }

        this.fileName = fileName;
        this.fileType = fileType;
        this.recordLength = recordLength;
        this.content = Arrays.copyOf(content, content.length);

        this.flags = flags;
        this.recordsPerSector = recordsPerSector;
        this.size = size;
        this.timestamp = timestamp;
    }

    // ============================================================
    //  ACCESSORS
    // ============================================================

    public String getFileName() {
        return fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public int getRecordLength() {
        return recordLength;
    }

    public byte[] getContent() {
        return Arrays.copyOf(content, content.length);
    }

    public int getFlags() {
        return flags;
    }

    public int getRecordsPerSector() {
        return recordsPerSector;
    }

    public int getSize() {
        return size;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
