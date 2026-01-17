package com.miriki.ti99.imagetools.domain.io;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.miriki.ti99.imagetools.domain.*;
import com.miriki.ti99.imagetools.io.*;

/**
 * Initializes a new TI-99 disk image according to its DiskFormat.
 *
 * Steps:
 *   1. Clear all sectors (0x00)
 *   2. Initialize AllocationBitmap (mark VIB + FDI + FDR zone as used)
 *   3. Write VIB (Volume Information Block)
 *   4. Write empty FDI (File Descriptor Index)
 *   5. Write empty FDR zone (all FDR sectors = 0x00)
 */
public final class ImageFormatter {

    private static final Logger log = LoggerFactory.getLogger(ImageFormatter.class);
	
    private static final byte ERASED = (byte) 0xE5;

    private ImageFormatter() {
        // Utility class – no instances
    }

    // ============================================================
    //  PUBLIC API
    // ============================================================

    /**
     * Formats the given image according to its DiskFormat.
     */
    public static void initialize(Ti99Image image) {

        DiskFormat fmt = image.getFormat();
        int totalSectors = fmt.getTotalSectors();

        // 1. Clear all sectors
        clearAllSectors(image, totalSectors);

        // 2. Initialize ABM (mark system sectors)
        AllocationBitmap abm = initializeAllocationBitmap(fmt);

        // 3. Write VIB
        writeVib(image, fmt, abm);

        // 4. Write empty FDI
        writeEmptyFdi(image, fmt);

        // 5. Write empty FDR zone
        writeEmptyFdrZone(image, fmt);
    }

    // ============================================================
    //  STEP 1 – CLEAR ALL SECTORS
    // ============================================================

    private static void clearAllSectors(Ti99Image image, int totalSectors) {
        byte[] empty = new byte[Sector.SIZE];
        Arrays.fill(empty, ERASED);

        for (int sec = 0; sec < totalSectors; sec++) {
            image.getSector(sec).setData(empty);
        }
    }

    // ============================================================
    //  STEP 2 – INITIALIZE ALLOCATION BITMAP
    // ============================================================

    private static AllocationBitmap initializeAllocationBitmap(DiskFormat fmt) {

        int total = fmt.getTotalSectors();
        AllocationBitmap abm = new AllocationBitmap(total);

        // System sectors
        abm.allocate(fmt.getVibSector()); // VIB (usually 0)
        abm.allocate(fmt.getFdiSector()); // FDI (usually 1)

        // FDR zone (directory zone)
        // int first = fmt.getFirstFdrSector();
        // int count = fmt.getFdrSectorCount();
        /*
        // FDR zone is reserved but NOT allocated
		// Do NOT mark FDR sectors as used here.
        for (int i = 0; i < count; i++) {
            abm.allocate(first + i);
        }
        */

        return abm;
    }

    // ============================================================
    //  STEP 3 – WRITE VIB
    // ============================================================

    private static void writeVib(Ti99Image image, DiskFormat fmt, AllocationBitmap abm) {

    	// log.debug( "writeVib( image='{}', fmt='{}', abm='{}' )", image.toString(), fmt.toString(), abm.toString() );
    	
    	log.trace( "  volume:             '{}'", "NEWVOLUME" );
    	log.trace( "  getTotalSectors:    '{}'", fmt.getTotalSectors() );
    	log.trace( "  getSectorsPerTrack: '{}'", fmt.getSectorsPerTrack() );
    	log.trace( "  getTracksPerSide:   '{}'", fmt.getTracksPerSide() );
    	log.trace( "  getSides:           '{}'", fmt.getSides() );
    	log.trace( "  getDensity.ordinal: '{}'", fmt.getDensity().ordinal() );
    	log.trace( "  id:                 '{}'", "DSK" );
    	log.trace( "  create:             '{}'", 0 );
    	log.trace( "  update:             '{}'", 0 );
    	log.trace( "  backup:             '{}'", 0 );
    	log.trace( "  abm:                '{}'", abm.toBytes() );
    	
        VolumeInformationBlock vib = new VolumeInformationBlock(
                "NEWVOLUME",                   // default name
                fmt.getTotalSectors(),
                fmt.getSectorsPerTrack(),
                fmt.getTracksPerSide(),
                fmt.getSides(),
                fmt.getDensity().ordinal(),
                "DSK",                         // disk type
                "", 0,                         // creation timestamp
                "", 0,                         // update timestamp
                "", 0,                         // backup timestamp
                abm
        );

        Sector vibSector = image.getSector(fmt.getVibSector());
        VolumeInformationBlockIO.writeTo(vibSector, vib);
    }

    // ============================================================
    //  STEP 4 – WRITE EMPTY FDI
    // ============================================================

    private static void writeEmptyFdi(Ti99Image image, DiskFormat fmt) {

        // FDI is a 256-byte sector containing up to 127 2-byte pointers
        byte[] empty = new byte[Sector.SIZE];
        Arrays.fill(empty, (byte) 0x00);
        FileDescriptorIndex fdi = new FileDescriptorIndex(empty);

        Sector fdiSector = image.getSector(fmt.getFdiSector());
        FileDescriptorIndexIO.writeTo(fdiSector, fdi);
    }

    // ============================================================
    //  STEP 5 – WRITE EMPTY FDR ZONE
    // ============================================================

    private static void writeEmptyFdrZone(Ti99Image image, DiskFormat fmt) {

        int first = fmt.getFirstFdrSector();
        int count = fmt.getFdrSectorCount();

        byte[] empty = new byte[Sector.SIZE];
        Arrays.fill(empty, (byte) 0x00);

        for (int i = 0; i < count; i++) {
            Sector sector = image.getSector(first + i);
            sector.setData(empty);
        }
    }
}
