package com.miriki.ti99.imagetools.domain;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.miriki.ti99.imagetools.dto.FileDescriptorIndexDTO;
import com.miriki.ti99.imagetools.io.Sector;

/**
 * Represents the File Descriptor Index (FDI) sector of a TI-99 disk.
 *
 * The FDI is a 256-byte table containing up to 127 entries (2 bytes each),
 * where each entry stores the sector number of a File Descriptor Record (FDR).
 *
 * Layout (256 bytes):
 *   [0..1]   FDR sector #0
 *   [2..3]   FDR sector #1
 *   ...
 *   [254..255] last entry
 *
 * A value of 0x0000 marks an unused entry.
 */
public final class FileDescriptorIndex {

    @SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(FileDescriptorIndex.class);

    /** Maximum number of FDR entries in a 256-byte sector (minus terminator). */
    public static final int MAX_ENTRIES = (Sector.SIZE / 2) - 1;

    /** Raw 256-byte FDI table. */
    private final byte[] entries;

    // ============================================================
    //  CONSTRUCTOR
    // ============================================================

    /**
     * Creates a FileDescriptorIndex from a raw 256-byte array.
     *
     * @param entries raw FDI bytes (must be exactly 256 bytes)
     */
    public FileDescriptorIndex(byte[] entries) {
        // log.debug("[constructor] FileDescriptorIndex({})", entries);

        if (entries.length != Sector.SIZE) {
            throw new IllegalArgumentException("FDI must be exactly 256 bytes");
        }

        this.entries = Arrays.copyOf(entries, entries.length);
    }

    // ============================================================
    //  BASIC ACCESSORS
    // ============================================================

    /**
     * Returns a defensive copy of the raw 256-byte FDI table.
     */
    public byte[] getEntries() {
        // log.debug("getEntries()");
        return Arrays.copyOf(entries, entries.length);
    }

    /**
     * Returns all FDR sector numbers as an int array.
     * Unused entries will be 0.
     */
    public int[] getFdrSectors() {
        // log.debug("getFdrSectors()");

        int[] sectors = new int[MAX_ENTRIES];

        for (int i = 0; i < MAX_ENTRIES; i++) {
            int hi = entries[i * 2] & 0xFF;
            int lo = entries[i * 2 + 1] & 0xFF;
            sectors[i] = (hi << 8) | lo;
        }

        return sectors;
    }

    // ============================================================
    //  DOMAIN LOGIC – INDEXED ACCESS
    // ============================================================

    /**
     * Returns the FDR sector number for the given file index.
     */
    public int getFdrSectorForFile(int fileIndex) {
        // log.debug("getFdrSectorForFile({})", fileIndex);

        int offset = fileIndex * 2;
        int hi = entries[offset] & 0xFF;
        int lo = entries[offset + 1] & 0xFF;
        return (hi << 8) | lo;
    }

    /**
     * Sets the FDR sector number for the given file index.
     */
    public void setFdrSectorForFile(int fileIndex, int sector) {
        // log.debug("setFdrSectorForFile({}, {})", fileIndex, sector);

        int offset = fileIndex * 2;
        entries[offset]     = (byte) ((sector >> 8) & 0xFF);
        entries[offset + 1] = (byte) (sector & 0xFF);
    }

    // ============================================================
    //  FACTORY – EMPTY FDI
    // ============================================================

    /**
     * Creates an empty FDI (all entries = 0x0000).
     */
    public static FileDescriptorIndex createEmpty() {
        byte[] raw = new byte[Sector.SIZE]; // initialized to zeros
        return new FileDescriptorIndex(raw);
    }

    // ============================================================
    //  DTO CONVERSION
    // ============================================================

    /**
     * Converts this FDI into a DTO.
     */
    public FileDescriptorIndexDTO toDTO() {
        // log.debug("toDTO()");
        return new FileDescriptorIndexDTO(entries);
    }

    /**
     * Creates an FDI from a DTO.
     */
    public static FileDescriptorIndex fromDTO(FileDescriptorIndexDTO dto) {
        // log.debug("fromDTO({})", dto);
        return new FileDescriptorIndex(dto.getEntries());
    }
}
