package com.miriki.ti99.dskimg.domain;

import java.util.Arrays;
import java.util.Objects;

import com.miriki.ti99.dskimg.domain.enums.FileType;
import com.miriki.ti99.dskimg.domain.enums.RecordFormat;
import com.miriki.ti99.dskimg.dto.Ti99FileDTO;

/**
 * High-level representation of a TI‑99 file.
 *
 * This class is *not* the on‑disk FDR. Instead, it represents a fully
 * materialized file as used by the application layer.
 *
 * Three conceptual layers are strictly separated:
 *   1. TFI‑FLAGS  (FIAD/TIFILES header flags)
 *   2. SEMANTICS  (FileType + RecordFormat)
 *   3. FDR‑STATUSBYTE (HFDC bits 7,6,5,4,3,1,0)
 */
public final class Ti99File {

    // ============================================================
    //  FIELDS
    // ============================================================

    private String fileName;
    private String fileTypeLabel;     // UI/DTO only

    private byte[] content;           // unpacked FIAD/TIFILES content
    private byte[] packedContent;     // data as written to disk image

    private int flags;                // TIFILES header flags
    private int recordLength;         // 0 = PROGRAM
    private int recordsPerSector;     // from FDR
    private int dirSize;              // size reported by FDR
    private long timestamp;

    // Modern semantic fields
    private FileType type;            // PGM, DIS, INT
    private RecordFormat format;      // FIX, VAR

    // ============================================================
    //  CONSTRUCTORS
    // ============================================================

    public Ti99File() {}

    public Ti99File(String filename, String fileTypeLabel, int recordLength, byte[] content) {

        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("Filename must not be empty");
        }
        if (fileTypeLabel == null || fileTypeLabel.isBlank()) {
            throw new IllegalArgumentException("File type label must not be empty");
        }
        if (recordLength < 0) {
            throw new IllegalArgumentException("Record length must be >= 0");
        }

        this.fileName = filename.trim();
        this.fileTypeLabel = fileTypeLabel.trim();
        this.recordLength = recordLength;
        this.content = (content != null) ? Arrays.copyOf(content, content.length) : new byte[0];
    }

    // ============================================================
    //  SEMANTIC PROPERTIES
    // ============================================================

    public boolean isProgram()  { return type == FileType.PGM; }
    public boolean isInternal() { return type == FileType.INT; }
    public boolean isDisplay()  { return type == FileType.DIS; }

    public boolean isVariable() { return format == RecordFormat.VAR; }
    public boolean isFixed()    { return format == RecordFormat.FIX; }

    // ============================================================
    //  TFI‑FLAGS (FIAD/TIFILES HEADER)
    // ============================================================

    public boolean tfiIsProgram()     { return (flags & 0x01) != 0; }
    public boolean tfiIsInternal()    { return (flags & 0x02) != 0; }
    public boolean tfiIsProtected()   { return (flags & 0x08) != 0; }
    public boolean tfiIsBackup()      { return (flags & 0x10) != 0; }
    public boolean tfiIsEmulated()    { return (flags & 0x20) != 0; }
    public boolean tfiIsVariable()    { return (flags & 0x80) != 0; }

    // ============================================================
    //  STATUS BYTE (HFDC)
    // ============================================================

    public byte toFdrStatusByte() {

        int status = 0;

        // Bit 0: Program/Data
        if (isProgram()) status |= 0x01;

        // Bit 1: Binary/ASCII
        // DISPLAY = ASCII, INTERNAL/PROGRAM = Binary
        if (isInternal() || isProgram()) status |= 0x02;

        // Bit 3: Protected
        if (tfiIsProtected()) status |= 0x08;

        // Bit 4: Backup
        if (tfiIsBackup()) status |= 0x10;

        // Bit 5: Emulate
        if (tfiIsEmulated()) status |= 0x20;

        // Bit 7: Variable
        if (isVariable()) status |= 0x80;

        return (byte) status;
    }

    // ============================================================
    //  PACKED CONTENT ACCESSORS
    // ============================================================

    public void setPackedContent(byte[] data) {
        this.packedContent = (data != null) ? Arrays.copyOf(data, data.length) : null;
    }

    public byte[] getPackedContent() {
        if (packedContent != null) {
            return Arrays.copyOf(packedContent, packedContent.length);
        }
        return Arrays.copyOf(content, content.length);
    }

    // ============================================================
    //  SETTERS
    // ============================================================

    public void setFileName(String name) { this.fileName = Objects.requireNonNull(name).trim(); }
    public void setFileTypeLabel(String type) { this.fileTypeLabel = Objects.requireNonNull(type).trim(); }

    public void setContent(byte[] data) {
        this.content = (data != null) ? Arrays.copyOf(data, data.length) : new byte[0];
    }

    public void setFlags(int flags) { this.flags = flags; }
    public void setRecordLength(int len) { this.recordLength = len; }
    public void setRecordsPerSector(int rps) { this.recordsPerSector = rps; }
    public void setTimestamp(long ts) { this.timestamp = ts; }
    public void setDirSize(int size) { this.dirSize = size; }

    public void setType(FileType t) { this.type = t; }
    public void setFormat(RecordFormat f) { this.format = f; }

    // ============================================================
    //  GETTERS
    // ============================================================

    public String getFileName() { return fileName; }
    public String getFileTypeLabel() { return fileTypeLabel; }

    public FileType getType() { return type; }
    public RecordFormat getFormat() { return format; }

    public byte[] getContent() { return Arrays.copyOf(content, content.length); }
    public int getFlags() { return flags; }
    public int getRecordLength() { return recordLength; }
    public int getRecordsPerSector() { return recordsPerSector; }
    public long getTimestamp() { return timestamp; }
    public int getDirSize() { return dirSize; }

    public int getLogicalSize() { return content.length; }

    // ============================================================
    //  DTO CONVERSION
    // ============================================================

    public Ti99FileDTO toDTO() {
        return new Ti99FileDTO(
                fileName,
                fileTypeLabel,
                recordLength,
                Arrays.copyOf(content, content.length),
                flags,
                recordsPerSector,
                timestamp
        );
    }

    public static Ti99File fromDTO(Ti99FileDTO dto) {

        byte[] content = dto.getContent();

        Ti99File file = new Ti99File(
                dto.getFileName(),
                dto.getFileType(),
                dto.getRecordLength(),
                content
        );

        file.setFlags(dto.getFlags());
        file.setRecordsPerSector(dto.getRecordsPerSector());
        file.setDirSize(content.length);
        file.setTimestamp(dto.getTimestamp());

        return file;
    }

    // ============================================================
    //  DEBUG STRING
    // ============================================================

    @Override
    public String toString() {
        return "Ti99File{" +
                "name='" + fileName + '\'' +
                ", type=" + type +
                ", format=" + format +
                ", logicalSize=" + getLogicalSize() +
                ", dirSize=" + dirSize +
                ", flags=" + flags +
                ", timestamp=" + timestamp +
                '}';
    }
}
