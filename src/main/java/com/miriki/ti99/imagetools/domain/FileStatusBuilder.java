package com.miriki.ti99.imagetools.domain;

public final class FileStatusBuilder {

    public static int buildStatusByte(Ti99File file) {
        int status = 0;

        if (file.isProgram())   status |= 0x01;  // Bit 0
        if (file.isInternal())  status |= 0x02;  // Bit 1
        if (file.tfiIsProtected()) status |= 0x08;
        if (file.tfiIsBackup())    status |= 0x10;
        if (file.tfiIsEmulated())  status |= 0x20;
        if (file.isVariable())     status |= 0x80;

        return status;
    }
}
