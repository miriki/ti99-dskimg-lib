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
 *   - file type (string form, UI/DTO only)
 *   - record length + records per sector
 *   - raw FIAD content (unpacked)
 *   - packed content (for writing to disk images)
 *   - FIAD/TIFILES flags (TFI)
 *   - timestamps
 *   - directory size (from FDR)
 *
 * Three conceptual layers are strictly separated:
 *
 *   1. TFI‑FLAGS  (FIAD/TIFILES header flags) → tfiIsXxx()
 *   2. FDR‑TYPES  (TI‑DOS on‑disk type codes) → fdrIsXxx()
 *   3. SEMANTICS  (logical meaning of the file) → isXxx()
 */
public final class Ti99File {

    private static final Logger log = LoggerFactory.getLogger(Ti99File.class);

    // ============================================================
    //  FIELDS
    // ============================================================

    private String fileName;
    private String fileType;          // UI/DTO only
    private byte[] content;           // FIAD raw content (unpacked)

    private int flags;                // TIFILES header flags (TFI)
    private int recordLength;         // 0 = PROGRAM
    private int recordsPerSector;     // from FDR
    private int dirSize;              // size reported by FDR
    private long timestamp;

    private int fdrType;              // TI‑DOS type code (1–5)

    private byte[] packedContent;     // data as written to disk image

    // ============================================================
    //  PACKED CONTENT ACCESSORS
    // ============================================================

    public void setPackedContent(byte[] data) {
        this.packedContent = data;
    }

    public byte[] getPackedContent() {
        return (packedContent != null) ? packedContent : content;
    }

    // ============================================================
    //  TFI‑FLAGS (FIAD/TIFILES HEADER)
    // ============================================================

    /** FIAD: PROGRAM flag (bit 0x01) — rarely used, actual program detection via recordLength == 0 */
    public boolean tfiIsProgram()     { return (flags & 0x01) != 0; }
    public boolean tfiIsData()        { return !tfiIsProgram(); }

    /** FIAD: INTERNAL flag (bit 0x02) — INT/FIX or INT/VAR */
    public boolean tfiIsInternal()    { return (flags & 0x02) != 0; }
    public boolean tfiIsDisplay()     { return !tfiIsInternal(); }

    /** FIAD: MODIFIED flag (bit 0x04) — file has been changed */
    // public boolean tfiIsModified()   { return (flags & 0x04) != 0; }

    /** FIAD: UNMODIFIED flag (bit 0x08) — file is unchanged (rarely used) */
    // public boolean tfiIsUnmodified() { return (flags & 0x08) != 0; }
    // FIAD bit 0x08 = UNMODIFIED (TI), but used as PROTECT in Myarc/HFDC
    public boolean tfiIsProtected()   { return (flags & 0x08) != 0; }
    public boolean tfiIsUnprotected() { return !tfiIsProtected(); }

    /** FIAD: BACKUP flag (bit 0x10) — file is a backup copy */
    public boolean tfiIsBackup()      { return (flags & 0x10) != 0; }
    public boolean tfiIsNoBackup()    { return !tfiIsBackup(); }

    /** FIAD: EMULATE flag (bit 0x20) — file is emulator-specific */
    public boolean tfiIsEmulated()    { return (flags & 0x20) != 0; }
    public boolean tfiIsNotEmulated() { return !tfiIsEmulated(); }

    /** FIAD: FIXED flag (bit 0x40) — file has fixed record length */
    // public boolean tfiIsFixed()       { return (flags & 0x40) != 0; }

    /** FIAD: VARIABLE flag (bit 0x80) — file has variable record length */
    // public boolean tfiIsVariable()    { return (flags & 0x80) != 0; }
    public boolean tfiIsVariable()    { return (flags & 0x80) != 0; }
    public boolean tfiIsFixed()       { return !tfiIsVariable(); }

    // ============================================================
    //  SEMANTIC PROPERTIES (LOGICAL FILE MEANING)
    // ============================================================

    /** PROGRAM = recordLength == 0 */
    public boolean isProgram() {
        return recordLength == 0;
    }

    /** INTERNAL = FIAD flag */
    public boolean isInternal() {
        return tfiIsInternal();
    }

    public boolean isDisplay() {
        return !tfiIsInternal();
    }

    /** VARIABLE = FIAD flag */
    public boolean isVariable() {
        return tfiIsVariable();
    }

    /** FIXED = not variable */
    public boolean isFixed() {
        return !isVariable();
    }

    // ============================================================
    //  FDR‑TYPE ACCESSORS (ON‑DISK TYPE CODES)
    // ============================================================

    public void setFdrType(int type) {
        this.fdrType = type;
    }

    public int getFdrType() {
        return fdrType;
    }

    public boolean fdrIsProgram() { return fdrType == 0x01; }
    public boolean fdrIsDisFix()  { return fdrType == 0x02; }
    public boolean fdrIsDisVar()  { return fdrType == 0x03; }
    public boolean fdrIsIntFix()  { return fdrType == 0x04; }
    public boolean fdrIsIntVar()  { return fdrType == 0x05; }

    // ============================================================
    //  CONSTRUCTORS
    // ============================================================

    public Ti99File() {}

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
    public int getDirSize() { return dirSize; }

    /** Actual size (if content loaded), otherwise directory size. */
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
                ", fdrType=" + fdrType +
                ", timestamp=" + timestamp +
                '}';
    }
}
