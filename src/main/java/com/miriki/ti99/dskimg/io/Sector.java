package com.miriki.ti99.dskimg.io;

import java.util.Arrays;
import java.util.Objects;

import com.miriki.ti99.dskimg.domain.constants.DiskLayoutConstants;

/**
 * Represents a 256-byte sector inside a TI-99 disk image.
 *
 * A Sector is a view into the underlying image byte array. It does not copy
 * the data on construction; instead it exposes read/write access to the
 * corresponding 256-byte slice.
 */
public final class Sector {

    private final byte[] imageBytes; // full disk image
    private final int offset;        // byte offset of this sector in the image

    // ============================================================
    //  CONSTRUCTOR
    // ============================================================

    public Sector(byte[] imageBytes, int sectorIndex) {
        Objects.requireNonNull(imageBytes, "imageBytes must not be null");

        int off = sectorIndex * DiskLayoutConstants.SECTOR_SIZE;
        if (off < 0 || off + DiskLayoutConstants.SECTOR_SIZE > imageBytes.length) {
            throw new IndexOutOfBoundsException(
                    "Sector " + sectorIndex + " out of bounds for image size " + imageBytes.length
            );
        }

        this.imageBytes = imageBytes;
        this.offset = off;
    }

    // ============================================================
    //  READ ACCESS
    // ============================================================

    /** Returns a defensive copy of the 256 bytes of this sector. */
    public byte[] getBytes() {
        return Arrays.copyOfRange(imageBytes, offset, offset + DiskLayoutConstants.SECTOR_SIZE);
    }

    /** Reads a single byte from the sector. */
    public byte get(int index) {
        checkIndex(index);
        return imageBytes[offset + index];
    }

    /** Returns the fixed sector size (256 bytes). */
    public int getSize() {
        return DiskLayoutConstants.SECTOR_SIZE;
    }

    // ============================================================
    //  WRITE ACCESS
    // ============================================================

    /** Overwrites the entire sector with the given 256-byte array. */
    public void setData(byte[] data) {
        Objects.requireNonNull(data, "data must not be null");
        requireExactSize(data.length);
        System.arraycopy(data, 0, imageBytes, offset, DiskLayoutConstants.SECTOR_SIZE);
    }

    /** Writes a single byte at the given index. */
    public void set(int index, byte value) {
        checkIndex(index);
        imageBytes[offset + index] = value;
    }

    // ============================================================
    //  INTERNAL HELPERS
    // ============================================================

    private void checkIndex(int index) {
        if (index < 0 || index >= DiskLayoutConstants.SECTOR_SIZE) {
            throw new IndexOutOfBoundsException("Sector byte index " + index);
        }
    }

    private void requireExactSize(int length) {
        if (length != DiskLayoutConstants.SECTOR_SIZE) {
            throw new IllegalArgumentException(
                    "Sector data must be exactly " + DiskLayoutConstants.SECTOR_SIZE + " bytes"
            );
        }
    }
}
