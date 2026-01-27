package com.miriki.ti99.dskimg.domain.constants;

public final class FdrConstants {

	// ------------------------------------------------------------
	// File Descriptor Record (FDR)
	// ------------------------------------------------------------
	
	public static final int FDR_FIRST_SECTOR_SSSD = 2;
	
	/** Size of a File Descriptor Record (FDR). */
	// FDR occupies exactly one sector
	public static final int FDR_SIZE = DiskLayoutConstants.SECTOR_SIZE;
	
	// Note: Older docs mention 14 directory sectors (2–15),
	// but the full FDR zone is 32 sectors (2–33) per TI DOS / HFDC.
	public static final int FDR_SECTOR_COUNT_SSSD = 32;
	
	// Data area starts at sector 34 (after 32 FDR sectors)
	public static final int FIRST_DATA_SECTOR_SSSD = 34;
	
	/** File name (10 bytes). */
	public static final int FDR_FILE_NAME = 0;
	public static final int FDR_FILE_NAME_SIZE = 10;
	
	// Extended record length (2 bytes) at offset 10
	public static final int FDR_EXTENDED_RECORD_LENGTH = 10;
	
	/** File status byte. */
	public static final int FDR_FILE_STATUS = 12;
	
	/** Records per sector (1 byte). */
	public static final int FDR_RECORDS_PER_SECTOR = 13;
	
	/** Total number of sectors allocated by file (2 bytes). */
	public static final int FDR_TOTAL_SECTORS = 14;
	
	/** End-of-file offset (1 byte). */
	public static final int FDR_EOF_OFFSET = 16;
	
	/** Logical record length (1 byte). */
	public static final int FDR_LOGICAL_RECORD_LENGTH = 17;
	
	/** Level 3 records used (2 bytes, little-endian). */
	public static final int FDR_LEVEL3_RECORDS_USED = 18;
	public static final int FDR_LEVEL3_RECORDS_SIZE = 2;
	
	/** Timestamp: file created (4 bytes). */
	public static final int FDR_TIMESTAMP_CREATED = 20;
	public static final int FDR_TIMESTAMP_SIZE = 4;
	
	/** Timestamp: file last updated (4 bytes). */
	public static final int FDR_TIMESTAMP_UPDATED = 24;
	
	/** Offset of the Data Chain Pointer block (DCP) inside the FDR. */
	public static final int FDR_DCP_OFFSET = 28;
	
	/** Size of the Data Chain Pointer block (DCP). */
	public static final int FDR_DCP_SIZE = 228;

	// --------------------
	
    /** Size of a Data Chain Pointer entry (HFDC). */
    public static final int DCP_ENTRY_SIZE = 3;

	/** Maximum number of DCP entries (76 × 3 = 228 bytes) */
	public static final int DCP_ENTRY_COUNT = 76;

    /** Offset of the high byte of the start sector. */
    public static final int DCP_START_SECTOR_HI = 0;

    /** Offset of the low byte of the start sector. */
    public static final int DCP_START_SECTOR_LO = 1;

    /** Offset of the length nibble ((length-1)<<4). */
    public static final int DCP_LENGTH_NIBBLE = 2;

}
