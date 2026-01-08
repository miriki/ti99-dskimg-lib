package com.miriki.ti99.imagetools.domain;

import java.util.Arrays;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.miriki.ti99.imagetools.dto.Ti99FileDTO;

/**
 * High-level representation of a TI‑99 file.
 *
 * This class is *not* the on‑disk FDR. Instead, it represents a fully
 * materialized file as used by the application layer:
 *
 *   - logical TI filename
 *   - file type (string form)
 *   - record length + records per sector
 *   - raw file content
 *   - flags and timestamps
 *   - directory size (from FDR)
 *
 * It is used by import/export logic, editors, and UI components.
 */
public final class Ti99File {

    private static final Logger log = LoggerFactory.getLogger(Ti99File.class);

    // ============================================================
    //  FIELDS
    // ============================================================

    private String fileName;
    private String fileType;
    private byte[] content;

    private int flags;
    private int recordLength;
    private int recordsPerSector;
    private int dirSize;      // Size reported by FDR
    private long timestamp;

    // ============================================================
    //  CONSTRUCTORS
    // ============================================================

    /** Default constructor for frameworks / DTO usage. */
    public Ti99File() {}

    /**
     * Creates a high-level TI‑99 file object.
     *
     * @param filename      logical TI filename
     * @param fileType      TI file type (string form)
     * @param recordLength  logical record length
     * @param content       raw file content
     */
    public Ti99File(String filename, String fileType, int recordLength, byte[] content) {
        log.debug("[constructor] Ti99File({}, {}, {}, data[{}])",
                filename, fileType, recordLength,
                (content != null ? content.length : -1));

        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("Filename must not be empty");
        }
        if (fileType == null || fileType.isBlank()) {
            throw new IllegalArgumentException("File type must not be empty");
        }
        if (recordLength < 0) {
            throw new IllegalArgumentException("Record length must be >= 0");
        }

        this.fileName = filename;
        this.fileType = fileType;
        this.recordLength = recordLength;
        this.content = (content != null) ? Arrays.copyOf(content, content.length) : new byte[0];
    }

    // ============================================================
    //  SETTERS
    // ============================================================

    public void setFileName(String name) { this.fileName = Objects.requireNonNull(name); }
    public void setFileType(String type) { this.fileType = Objects.requireNonNull(type); }
    public void setContent(byte[] data) {
        this.content = (data != null) ? Arrays.copyOf(data, data.length) : new byte[0];
    }
    public void setFlags(int flags) { this.flags = flags; }
    public void setRecordLength(int len) { this.recordLength = len; }
    public void setRecordsPerSector(int rps) { this.recordsPerSector = rps; }
    public void setTimestamp(long ts) { this.timestamp = ts; }
    public void setDirSize(int size) { this.dirSize = size; }

    // ============================================================
    //  GETTERS
    // ============================================================

    public String getFileName() { return fileName; }
    public String getFileType() { return fileType; }
    public byte[] getContent() { return Arrays.copyOf(content, content.length); }
    public int getFlags() { return flags; }
    public int getRecordLength() { return recordLength; }
    public int getRecordsPerSector() { return recordsPerSector; }
    public long getTimestamp() { return timestamp; }

    /** Size reported by the FDR (directory size). */
    public int getDirSize() { return dirSize; }

    /**
     * Actual size (if content loaded), otherwise directory size.
     */
    public int getSize() {
        return (content != null) ? content.length : dirSize;
    }

    // ============================================================
    //  DTO CONVERSION
    // ============================================================

    public Ti99FileDTO toDTO() {
        log.debug("toDTO()");
        return new Ti99FileDTO(
                fileName,
                fileType,
                recordLength,
                Arrays.copyOf(content, content.length),
                flags,
                recordsPerSector,
                dirSize,
                timestamp
        );
    }

    public static Ti99File fromDTO(Ti99FileDTO dto) {
        log.debug("fromDTO({})", dto);

        Ti99File file = new Ti99File(
                dto.getFileName(),
                dto.getFileType(),
                dto.getRecordLength(),
                dto.getContent()
        );
        file.setFlags(dto.getFlags());
        file.setRecordsPerSector(dto.getRecordsPerSector());
        file.setDirSize(dto.getSize());
        file.setTimestamp(dto.getTimestamp());
        return file;
    }

    // ============================================================
    //  DEBUG STRING
    // ============================================================

    @Override
    public String toString() {
        log.debug("toString()");
        return "Ti99File{" +
                "name='" + fileName + '\'' +
                ", type='" + fileType + '\'' +
                ", size=" + getSize() +
                ", flags=" + flags +
                ", timestamp=" + timestamp +
                '}';
    }
}
