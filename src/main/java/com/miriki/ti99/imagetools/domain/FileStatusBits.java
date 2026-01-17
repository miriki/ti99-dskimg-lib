package com.miriki.ti99.imagetools.domain;

public enum FileStatusBits {

    PROGRAM   (0x01), // Bit 0: 1 = Program, 0 = Data
    INTERNAL  (0x02), // Bit 1: 1 = Internal, 0 = Display
    PROTECT   (0x08), // Bit 3
    BACKUP    (0x10), // Bit 4
    EMULATE   (0x20), // Bit 5
    VARIABLE  (0x80); // Bit 7: 1 = Variable, 0 = Fixed

    public final int mask;

    FileStatusBits(int mask) {
        this.mask = mask;
    }
}
