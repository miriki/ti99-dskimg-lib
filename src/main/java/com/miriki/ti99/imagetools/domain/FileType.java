package com.miriki.ti99.imagetools.domain;

public enum FileType {
    PROGRAM,
    DISFIX,
    DISVAR,
    INTFIX,
    INTVAR,
    UNKNOWN;

	public static FileType fromStatus(int status) {

	    boolean isProgram  = (status & 0x01) != 0;  // Bit 0
	    boolean isInternal = (status & 0x02) != 0;  // Bit 1
	    boolean isVariable = (status & 0x80) != 0;  // Bit 7

	    if (isProgram) return PROGRAM;

	    if (!isInternal && !isVariable) return DISFIX;
	    if (!isInternal &&  isVariable) return DISVAR;
	    if ( isInternal && !isVariable) return INTFIX;
	    if ( isInternal &&  isVariable) return INTVAR;

	    return UNKNOWN;
	}
}
