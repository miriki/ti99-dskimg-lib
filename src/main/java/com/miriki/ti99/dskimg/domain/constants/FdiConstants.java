package com.miriki.ti99.dskimg.domain.constants;

public final class FdiConstants {

    // ------------------------------------------------------------
    // File Descriptor Index (FDI) – sector 1
    // ------------------------------------------------------------

    /** Sector number of the FDI. */
    public static final int FDI_SECTOR = 1;

    public static final int FDI_ENTRY_COUNT = 127;
    
    /** Size of the FDI in bytes. */
    public static final int FDI_SIZE = DiskLayoutConstants.SECTOR_SIZE;
	public static final int FDI_ENTRY_SIZE = 2;

}
