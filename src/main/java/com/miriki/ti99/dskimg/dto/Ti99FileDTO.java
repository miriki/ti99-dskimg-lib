package com.miriki.ti99.dskimg.dto;

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

    private final String fileName;
    private final String fileType;
    private final int recordLength;
    private final byte[] content;

    private final int flags;
    private final int recordsPerSector;
    // private final int size;
    private final long timestamp;

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

    /*
    public int getSize() {
        return content.length;
    }
    */

    public long getTimestamp() {
        return timestamp;
    }

    // ============================================================
    //  OBJECT METHODS
    // ============================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ti99FileDTO other)) return false;
        return recordLength == other.recordLength
                && flags == other.flags
                && recordsPerSector == other.recordsPerSector
                && content.length == other.content.length
                && timestamp == other.timestamp
                && fileName.equals(other.fileName)
                && fileType.equals(other.fileType)
                && Arrays.equals(content, other.content);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(fileName, fileType, recordLength, flags, recordsPerSector, content.length, timestamp);
        result = 31 * result + Arrays.hashCode(content);
        return result;
    }

    @Override
    public String toString() {
        return "Ti99FileDTO[" +
                "name=" + fileName +
                ", type=" + fileType +
                ", size=" + content.length +
                ", recordLength=" + recordLength +
                ", flags=" + flags +
                ", timestamp=" + timestamp +
                "]";
    }
}
