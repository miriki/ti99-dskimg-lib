package com.miriki.ti99.dskimg.domain;

import java.util.EnumSet;
import java.util.Objects;

import com.miriki.ti99.dskimg.domain.enums.FileType;
import com.miriki.ti99.dskimg.domain.enums.RecordFormat;

public final class FileStatusBuilder {

    private FileStatusBuilder() {}

    /**
     * Builds the FDR status byte for a Ti99File using the modern FdrStatus API.
     */
    public static int buildStatusByte(Ti99File file) {
        Objects.requireNonNull(file, "file must not be null");

        // ------------------------------------------------------------
        // Determine FileType (bits 0–1)
        // ------------------------------------------------------------
        FileType type;
        if (file.isProgram()) {
            type = FileType.PGM;
        } else if (file.isInternal()) {
            type = FileType.INT;
        } else {
            type = FileType.DIS;
        }

        // ------------------------------------------------------------
        // Determine RecordFormat (bit 7)
        // ------------------------------------------------------------
        RecordFormat format = file.isVariable()
                ? RecordFormat.VAR
                : RecordFormat.FIX;

        // ------------------------------------------------------------
        // Determine FileAttributes (bits 3–5)
        // ------------------------------------------------------------
        EnumSet<FileAttribute> attrs = EnumSet.noneOf(FileAttribute.class);

        if (file.tfiIsProtected()) attrs.add(FileAttribute.PROTECTED);
        if (file.tfiIsBackup())    attrs.add(FileAttribute.BACKUP);
        if (file.tfiIsEmulated())  attrs.add(FileAttribute.EMULATED);

        // ------------------------------------------------------------
        // Delegate to FdrStatus for bit encoding
        // ------------------------------------------------------------
        return new FdrStatus(type, format, attrs).toByte();
    }
}
