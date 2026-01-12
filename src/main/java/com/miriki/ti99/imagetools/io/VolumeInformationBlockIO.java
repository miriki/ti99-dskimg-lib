package com.miriki.ti99.imagetools.io;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.miriki.ti99.imagetools.domain.AllocationBitmap;
import com.miriki.ti99.imagetools.domain.VolumeInformationBlock;

/**
 * IO helper for reading and writing the 256-byte Volume Information Block (VIB).
 *
 * Layout summary:
 *   0x00–0x09 : Volume name (10 chars)
 *   0x0A–0x0B : Total sectors (big endian)
 *   0x0C      : Sectors per track
 *   0x0D–0x0F : Signature ("DSK")
 *   0x11      : Tracks per side
 *   0x12      : Sides
 *   0x13      : Density
 *   0x14–0x1D : Directory 1 name
 *   0x1E–0x1F : Directory 1 pointer
 *   0x20–0x29 : Directory 2 name
 *   0x2A–0x2B : Directory 2 pointer
 *   0x2C–0x35 : Directory 3 name
 *   0x36–0x37 : Directory 3 pointer
 *   0x38–...  : Allocation bitmap (variable length)
 */
public final class VolumeInformationBlockIO {

    private static final Logger log = LoggerFactory.getLogger(VolumeInformationBlockIO.class);

    private static final int SIZE = 256;
    private static final int OFFSET_BITMAP = 0x38;

    private VolumeInformationBlockIO() {
        // Utility class – no instances
    }

    // ============================================================
    //  READ
    // ============================================================

    public static VolumeInformationBlock readFrom(Sector sector) {
        Objects.requireNonNull(sector, "Sector must not be null");
        // log.debug("readFrom({})", sector);

        byte[] raw = sector.getBytes();

        // --- Basic disk info ---
        String volumeName     = readString(raw, 0x00, 10);
        int totalSectors      = readWord(raw, 0x0A);
        int sectorsPerTrack   = raw[0x0C] & 0xFF;

        // --- Signature ---
        String signature      = readString(raw, 0x0D, 3);

        // --- Geometry ---
        int tracksPerSide     = raw[0x11] & 0xFF;
        int sides             = raw[0x12] & 0xFF;
        int density           = raw[0x13] & 0xFF;

        // --- Directories ---
        String dir1Name       = readString(raw, 0x14, 10);
        int dir1Pointer       = readWord(raw, 0x1E);

        String dir2Name       = readString(raw, 0x20, 10);
        int dir2Pointer       = readWord(raw, 0x2A);

        String dir3Name       = readString(raw, 0x2C, 10);
        int dir3Pointer       = readWord(raw, 0x36);

        // --- Allocation Bitmap ---
        int bitmapBytes       = (totalSectors + 7) / 8;
        byte[] bitmapRaw      = Arrays.copyOfRange(raw, OFFSET_BITMAP, OFFSET_BITMAP + bitmapBytes);
        AllocationBitmap abm  = AllocationBitmap.fromBytes(bitmapRaw, totalSectors);

        return new VolumeInformationBlock(
                volumeName,
                totalSectors,
                sectorsPerTrack,
                tracksPerSide,
                sides,
                density,
                signature,
                dir1Name, dir1Pointer,
                dir2Name, dir2Pointer,
                dir3Name, dir3Pointer,
                abm
        );
    }

    // ============================================================
    //  WRITE
    // ============================================================

    public static void writeTo(Sector sector, VolumeInformationBlock vib) {
        Objects.requireNonNull(sector, "Sector must not be null");
        Objects.requireNonNull(vib, "VolumeInformationBlock must not be null");

        // log.debug("writeTo({}, {})", sector, vib);

        byte[] raw = new byte[SIZE];

        // --- Basic disk info ---
        writeString(raw, 0x00, 10, vib.getVolumeName());
        writeWord(raw, 0x0A, vib.getTotalSectors());
        raw[0x0C] = (byte) vib.getSectorsPerTrack();

        // --- Signature ---
        writeString(raw, 0x0D, 3, vib.getSignature());

        // --- Geometry ---
        raw[0x11] = (byte) vib.getTracksPerSide();
        raw[0x12] = (byte) vib.getSides();
        raw[0x13] = (byte) vib.getDensity();

        // --- Directories ---
        writeString(raw, 0x14, 10, vib.getDir1Name());
        writeWord(raw, 0x1E, vib.getDir1Pointer());

        writeString(raw, 0x20, 10, vib.getDir2Name());
        writeWord(raw, 0x2A, vib.getDir2Pointer());

        writeString(raw, 0x2C, 10, vib.getDir3Name());
        writeWord(raw, 0x36, vib.getDir3Pointer());

        // --- Allocation Bitmap ---
        int bitmapBytes = (vib.getTotalSectors() + 7) / 8;
        byte[] bitmapRaw = vib.getAbm().toBytes();

        System.arraycopy(bitmapRaw, 0, raw, OFFSET_BITMAP, bitmapBytes);

        // Fill unused bitmap area with 0xFF
        // Arrays.fill(raw, OFFSET_BITMAP + bitmapBytes, SIZE, (byte) 0xFF);
        // Arrays.fill(raw, OFFSET_BITMAP + bitmapBytes, OFFSET_BITMAP + bitmapBytes, (byte) 0x00);

        // --- Write to sector ---
        for (int i = 0; i < SIZE; i++) {
            sector.set(i, raw[i]);
        }
    }

    // ============================================================
    //  HELPERS
    // ============================================================

    private static String readString(byte[] raw, int offset, int len) {
        // log.debug("readString({}, {}, {})", raw, offset, len);
        return new String(raw, offset, len, StandardCharsets.US_ASCII).trim();
    }

    private static void writeString(byte[] raw, int offset, int len, String value) {
        // log.debug("writeString({}, {}, {}, {})", raw, offset, len, value);

        byte[] bytes = value.getBytes(StandardCharsets.US_ASCII);
        int copyLen = Math.min(bytes.length, len);

        System.arraycopy(bytes, 0, raw, offset, copyLen);

        // pad with spaces
        Arrays.fill(raw, offset + copyLen, offset + len, (byte) 0x20);
    }

    private static int readWord(byte[] raw, int offset) {
        // log.debug("readWord({}, {})", raw, offset);
        return ((raw[offset] & 0xFF) << 8) | (raw[offset + 1] & 0xFF);
    }

    private static void writeWord(byte[] raw, int offset, int value) {
        // log.debug("writeWord({}, {}, {})", raw, offset, value);
        raw[offset]     = (byte) ((value >> 8) & 0xFF);
        raw[offset + 1] = (byte) (value & 0xFF);
    }
}
