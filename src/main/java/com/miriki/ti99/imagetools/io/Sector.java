package com.miriki.ti99.imagetools.io;

import java.util.Arrays;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a 256-byte sector inside a TI-99 disk image.
 *
 * A Sector is a *view* into the underlying image byte array. It does not copy
 * the data on construction; instead it exposes read/write access to the
 * corresponding 256-byte slice.
 *
 * Responsibilities:
 *   - bounds-checked read/write access
 *   - defensive copies for full-sector reads
 *   - strict size enforcement for writes
 */
public final class Sector {

    private static final Logger log = LoggerFactory.getLogger(Sector.class);

    /** Fixed TI sector size (HFDC, TI-Controller, DSK images) */
    public static final int SIZE = 256;

    private final byte[] imageBytes; // full disk image
    private final int offset;        // byte offset of this sector in the image

    // ============================================================
    //  CONSTRUCTOR
    // ============================================================

    /**
     * Creates a sector view for the given sector index.
     *
     * @param imageBytes full disk image byte array
     * @param sectorIndex index of the sector (0-based)
     */
    public Sector(byte[] imageBytes, int sectorIndex) {
        Objects.requireNonNull(imageBytes, "imageBytes must not be null");

        log.debug("Sector(index={})", sectorIndex);

        int off = sectorIndex * SIZE;
        if (off < 0 || off + SIZE > imageBytes.length) {
            throw new IndexOutOfBoundsException(
                    "Sector " + sectorIndex + " out of bounds for image size " + imageBytes.length
            );
        }

        this.imageBytes = imageBytes;
        this.offset = off;

        log.info("Sector created at index {}", sectorIndex);
    }

    // ============================================================
    //  READ ACCESS
    // ============================================================

    /**
     * Returns a defensive copy of the 256 bytes of this sector.
     */
    public byte[] getBytes() {
        log.debug("getBytes()");
        return Arrays.copyOfRange(imageBytes, offset, offset + SIZE);
    }

    /**
     * Reads a single byte from the sector.
     */
    public byte get(int index) {
        log.debug("get({})", index);
        checkIndex(index);
        return imageBytes[offset + index];
    }

    /**
     * Returns the fixed sector size (256 bytes).
     */
    public int getSize() {
        return SIZE;
    }

    // ============================================================
    //  WRITE ACCESS
    // ============================================================

    /**
     * Overwrites the entire sector with the given 256-byte array.
     */
    public void setData(byte[] data) {
        Objects.requireNonNull(data, "data must not be null");
        log.debug("setData(len={})", data.length);

        requireExactSize(data.length);
        System.arraycopy(data, 0, imageBytes, offset, SIZE);
    }

    /**
     * Writes a single byte at the given index.
     */
    public void set(int index, byte value) {
        log.debug("set({}, {})", index, value);
        checkIndex(index);
        imageBytes[offset + index] = value;
    }

    // ============================================================
    //  INTERNAL HELPERS
    // ============================================================

    /**
     * Ensures the given index is within 0..255.
     */
    private void checkIndex(int index) {
        if (index < 0 || index >= SIZE) {
            throw new IndexOutOfBoundsException("Sector byte index " + index);
        }
    }

    /**
     * Ensures that a full-sector write uses exactly 256 bytes.
     */
    private void requireExactSize(int length) {
        if (length != SIZE) {
            throw new IllegalArgumentException(
                    "Sector data must be exactly " + SIZE + " bytes"
            );
        }
    }
}
