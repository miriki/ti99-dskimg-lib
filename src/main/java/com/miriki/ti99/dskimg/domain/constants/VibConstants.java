package com.miriki.ti99.dskimg.domain.constants;

public final class VibConstants {

	private VibConstants() {}
	
	// ------------------------------------------------------------
	// Volume Information Block (VIB) – sector 0
	// ------------------------------------------------------------

	/** Sector number of the VIB (always 0). */
	public static final int VIB_SECTOR = 0;
	
	/** Total size of the VIB in bytes. */
	public static final int VIB_SIZE = DiskLayoutConstants.SECTOR_SIZE;
	
	/** Disk name (10 bytes). */
	public static final int VIB_DISK_NAME = 0;
	public static final int VIB_DISK_NAME_SIZE = 10;
	
	/** Total sectors on disk (2 bytes). */
	public static final int VIB_TOTAL_SECTORS = 10;
	
	/** Sectors per track (1 byte). */
	public static final int VIB_SECTORS_PER_TRACK = 12;
	
	/** Disk identity string "DSK" (3 bytes). */
	public static final int VIB_DISK_IDENTITY = 13;
	
	/** Reserved byte. */
	public static final int VIB_RESERVED = 16;
	
	/** Tracks per side (1 byte). */
	public static final int VIB_TRACKS_PER_SIDE = 17;
	
	/** Number of sides (1=SS, 2=DS). */
	public static final int VIB_SIDES = 18;
	
	/** Density code (0=sd, 1=sd, 2=dd, 3=hd, 4=ud). */
	public static final int VIB_DENSITY = 19;
	
	/** Directory 1 name (10 bytes). */
	public static final int VIB_DIR1_NAME = 20;
	public static final int VIB_DIR_NAME_SIZE = 10;
	
	/** Directory 1 pointer to FDI (2 bytes). */
	public static final int VIB_DIR1_FDI_PTR = 30;
	public static final int VIB_DIR_PTR_SIZE = 2;
	
	/** Directory 2 name (10 bytes). */
	public static final int VIB_DIR2_NAME = 32;
	
	/** Directory 2 pointer to FDI (2 bytes). */
	public static final int VIB_DIR2_FDI_PTR = 42;
	
	/** Directory 3 name (10 bytes). */
	public static final int VIB_DIR3_NAME = 44;
	
	/** Directory 3 pointer to FDI (2 bytes). */
	public static final int VIB_DIR3_FDI_PTR = 54;
	
	/** Allocation bitmap offset inside VIB. */
	public static final int VIB_ABM_OFFSET = 56;
	
	/** Allocation bitmap size in bytes. */
	public static final int VIB_ABM_SIZE = 200;

	/** Maximum number of sectors representable in a TI‑99 ABM (200 bytes × 8 bits). */ 
	public static final int MAX_BITMAP_SECTORS = 1600; 	
}
