package com.miriki.ti99.dskimg.domain;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.miriki.ti99.dskimg.domain.constants.DiskLayoutConstants;
import com.miriki.ti99.dskimg.domain.constants.FdiConstants;
import com.miriki.ti99.dskimg.dto.FileDescriptorIndexDTO;

/**
 * Represents the File Descriptor Index (FDI) sector of a TI-99 disk.
 *
 * The FDI is a 256-byte table containing up to 128 entries (2 bytes each),
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

    /** Maximum number of FDR entries in a 256-byte sector. */
    public static final int MAX_ENTRIES = FdiConstants.FDI_ENTRY_COUNT;

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

        if (entries == null) {
            throw new IllegalArgumentException("FDI entries must not be null");
        }

        if (entries.length != DiskLayoutConstants.SECTOR_SIZE) {
            throw new IllegalArgumentException(
                    "FDI must be exactly " + DiskLayoutConstants.SECTOR_SIZE + " bytes");
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
        return Arrays.copyOf(entries, entries.length);
    }

    /**
     * Returns all FDR sector numbers as an int array.
     * Unused entries will be 0.
     */
    public int[] getFdrSectors() {

        int[] sectors = new int[MAX_ENTRIES];

        for (int i = 0; i < MAX_ENTRIES; i++) {
            int offset = i * FdiConstants.FDI_ENTRY_SIZE;
            int hi = entries[offset] & 0xFF;
            int lo = entries[offset + 1] & 0xFF;
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

        validateIndex(fileIndex);

        int offset = fileIndex * FdiConstants.FDI_ENTRY_SIZE;
        int hi = entries[offset] & 0xFF;
        int lo = entries[offset + 1] & 0xFF;
        return (hi << 8) | lo;
    }

    /**
     * Sets the FDR sector number for the given file index.
     */
    public void setFdrSectorForFile(int fileIndex, int sector) {

        validateIndex(fileIndex);

        int offset = fileIndex * FdiConstants.FDI_ENTRY_SIZE;
        entries[offset]     = (byte) ((sector >> 8) & 0xFF);
        entries[offset + 1] = (byte) (sector & 0xFF);
    }

    private void validateIndex(int index) {
        if (index < 0 || index >= MAX_ENTRIES) {
            throw new IndexOutOfBoundsException(
                    "FDI index " + index + " out of range 0.." + (MAX_ENTRIES - 1));
        }
    }

    // ============================================================
    //  FACTORY – EMPTY FDI
    // ============================================================

    /**
     * Creates an empty FDI (all entries = 0x0000).
     */
    public static FileDescriptorIndex createEmpty() {
        byte[] raw = new byte[DiskLayoutConstants.SECTOR_SIZE]; // initialized to zeros
        return new FileDescriptorIndex(raw);
    }

    // ============================================================
    //  DTO CONVERSION
    // ============================================================

    public FileDescriptorIndexDTO toDTO() {
        return new FileDescriptorIndexDTO(entries);
    }

    public static FileDescriptorIndex fromDTO(FileDescriptorIndexDTO dto) {
        return new FileDescriptorIndex(dto.getEntries());
    }
}
