package com.miriki.ti99.dskimg.domain;

import java.util.EnumSet;
import java.util.Objects;

import com.miriki.ti99.dskimg.domain.enums.FileType;
import com.miriki.ti99.dskimg.domain.enums.RecordFormat;

/**
 * Represents the decoded status byte of a TI‑99 File Descriptor Record (FDR).
 *
 * Encodes:
 *   - FileType        (bits 0–1)
 *   - FileAttributes  (bits 3–5)
 *   - RecordFormat    (bit 7)
 *
 * Immutable value object.
 */
public final class FdrStatus {

    // ============================================================
    //  BIT MASKS (TI‑99 standard)
    // ============================================================

    private static final int FILETYPE_MASK       = 0b00000011; // Bits 0–1
    // private static final int ATTRIBUTE_MASK      = 0b00111000; // Bits 3–5
    private static final int RECORD_FORMAT_MASK  = 0b10000000; // Bit 7

    // ============================================================
    //  FIELDS
    // ============================================================

    private final FileType type;
    private final RecordFormat format;
    private final EnumSet<FileAttribute> attributes;

    // ============================================================
    //  CONSTRUCTOR
    // ============================================================

    public FdrStatus(FileType type, RecordFormat format, EnumSet<FileAttribute> attributes) {
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.format = Objects.requireNonNull(format, "format must not be null");
        Objects.requireNonNull(attributes, "attributes must not be null");
        this.attributes = EnumSet.copyOf(attributes);
    }

    // ============================================================
    //  ACCESSORS
    // ============================================================

    public FileType getType() {
        return type;
    }

    public RecordFormat getFormat() {
        return format;
    }

    public EnumSet<FileAttribute> getAttributes() {
        return EnumSet.copyOf(attributes);
    }

    // ============================================================
    //  DECODE
    // ============================================================

    /**
     * Decodes an FDR status byte into a structured FdrStatus object.
     */
    public static FdrStatus fromByte(int status) {

        // FileType (bits 0–1)
        FileType type = FileType.fromCode(status & FILETYPE_MASK);

        // RecordFormat (bit 7)
        RecordFormat format =
                ((status & RECORD_FORMAT_MASK) != 0)
                ? RecordFormat.VAR
                : RecordFormat.FIX;

        // Attributes (bits 3–5)
        EnumSet<FileAttribute> attrs = EnumSet.noneOf(FileAttribute.class);
        for (FileAttribute a : FileAttribute.values()) {
            if ((status & a.mask) != 0) {
                attrs.add(a);
            }
        }

        return new FdrStatus(type, format, attrs);
    }

    // ============================================================
    //  ENCODE
    // ============================================================

    /**
     * Encodes this FdrStatus back into a raw TI‑99 status byte.
     */
    public int toByte() {
        int b = 0;

        // FileType
        b |= type.getCode();

        // RecordFormat
        if (format == RecordFormat.VAR) {
            b |= RECORD_FORMAT_MASK;
        }

        // Attributes
        for (FileAttribute a : attributes) {
            b |= a.mask;
        }

        return b;
    }

    // ============================================================
    //  DEBUG STRING
    // ============================================================

    @Override
    public String toString() {
        return "FdrStatus{" +
                "type=" + type +
                ", format=" + format +
                ", attributes=" + attributes +
                '}';
    }
}
