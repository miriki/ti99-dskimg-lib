package com.miriki.ti99.dskimg.io;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

import com.miriki.ti99.dskimg.domain.AllocationBitmap;
import com.miriki.ti99.dskimg.domain.VolumeInformationBlock;
import com.miriki.ti99.dskimg.domain.constants.DiskLayoutConstants;
import com.miriki.ti99.dskimg.domain.constants.VibConstants;

public final class VolumeInformationBlockIO {

    private VolumeInformationBlockIO() {}

    // ============================================================
    //  READ
    // ============================================================

    public static VolumeInformationBlock readFrom(Sector sector) {
        Objects.requireNonNull(sector, "sector must not be null");

        byte[] raw = sector.getBytes();

        String volumeName     = readString(raw, VibConstants.VIB_DISK_NAME, VibConstants.VIB_DISK_NAME_SIZE);
        int totalSectors      = readWord(raw, VibConstants.VIB_TOTAL_SECTORS);
        int sectorsPerTrack   = raw[VibConstants.VIB_SECTORS_PER_TRACK] & 0xFF;

        String signature      = readString(raw, VibConstants.VIB_DISK_IDENTITY, 3);

        int tracksPerSide     = raw[VibConstants.VIB_TRACKS_PER_SIDE] & 0xFF;
        int sides             = raw[VibConstants.VIB_SIDES] & 0xFF;
        int density           = raw[VibConstants.VIB_DENSITY] & 0xFF;

        String dir1Name       = readString(raw, VibConstants.VIB_DIR1_NAME, VibConstants.VIB_DIR_NAME_SIZE);
        int dir1Pointer       = readWord(raw, VibConstants.VIB_DIR1_FDI_PTR);

        String dir2Name       = readString(raw, VibConstants.VIB_DIR2_NAME, VibConstants.VIB_DIR_NAME_SIZE);
        int dir2Pointer       = readWord(raw, VibConstants.VIB_DIR2_FDI_PTR);

        String dir3Name       = readString(raw, VibConstants.VIB_DIR3_NAME, VibConstants.VIB_DIR_NAME_SIZE);
        int dir3Pointer       = readWord(raw, VibConstants.VIB_DIR3_FDI_PTR);

        // --- Allocation Bitmap ---
        int bitmapBytes = (totalSectors + 7) / 8;

        if (bitmapBytes > VibConstants.VIB_ABM_SIZE) {
            throw new IllegalStateException(
                    "VIB cannot represent " + totalSectors +
                    " sectors (max " + VibConstants.MAX_BITMAP_SECTORS + ")"
            );
        }

        byte[] bitmapRaw = Arrays.copyOfRange(
                raw,
                VibConstants.VIB_ABM_OFFSET,
                VibConstants.VIB_ABM_OFFSET + VibConstants.VIB_ABM_SIZE
        );

        AllocationBitmap abm = AllocationBitmap.fromBytes(bitmapRaw, totalSectors);

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
        Objects.requireNonNull(sector, "sector must not be null");
        Objects.requireNonNull(vib, "vib must not be null");

        byte[] raw = new byte[DiskLayoutConstants.SECTOR_SIZE];

        writeString(raw, VibConstants.VIB_DISK_NAME, VibConstants.VIB_DISK_NAME_SIZE, vib.getVolumeName());
        writeWord(raw, VibConstants.VIB_TOTAL_SECTORS, vib.getTotalSectors());
        raw[VibConstants.VIB_SECTORS_PER_TRACK] = (byte) vib.getSectorsPerTrack();

        writeString(raw, VibConstants.VIB_DISK_IDENTITY, 3, vib.getSignature());

        raw[VibConstants.VIB_TRACKS_PER_SIDE] = (byte) vib.getTracksPerSide();
        raw[VibConstants.VIB_SIDES]           = (byte) vib.getSides();
        raw[VibConstants.VIB_DENSITY]         = (byte) vib.getDensity();

        writeString(raw, VibConstants.VIB_DIR1_NAME, VibConstants.VIB_DIR_NAME_SIZE, vib.getDir1Name());
        writeWord(raw, VibConstants.VIB_DIR1_FDI_PTR, vib.getDir1Pointer());

        writeString(raw, VibConstants.VIB_DIR2_NAME, VibConstants.VIB_DIR_NAME_SIZE, vib.getDir2Name());
        writeWord(raw, VibConstants.VIB_DIR2_FDI_PTR, vib.getDir2Pointer());

        writeString(raw, VibConstants.VIB_DIR3_NAME, VibConstants.VIB_DIR_NAME_SIZE, vib.getDir3Name());
        writeWord(raw, VibConstants.VIB_DIR3_FDI_PTR, vib.getDir3Pointer());

     // --- Allocation Bitmap (HFDC: always 200 bytes) ---
        byte[] bitmapRaw = vib.getAbm().toBytes();
        int bitmapBytes  = (vib.getTotalSectors() + 7) / 8;

        // HFDC requires a fixed 200-byte bitmap
        if (bitmapRaw.length != VibConstants.VIB_ABM_SIZE) {
            throw new IllegalStateException(
                "Bitmap size mismatch: expected " + VibConstants.VIB_ABM_SIZE +
                " bytes (HFDC), got " + bitmapRaw.length
            );
        }

        // Copy actual bitmap (only the part that corresponds to real sectors)
        System.arraycopy(bitmapRaw, 0, raw, VibConstants.VIB_ABM_OFFSET, bitmapBytes);

        // Fill remaining reserved area with 0xFF (HFDC: unused bits = allocated)
        Arrays.fill(
                raw,
                VibConstants.VIB_ABM_OFFSET + bitmapBytes,
                VibConstants.VIB_ABM_OFFSET + VibConstants.VIB_ABM_SIZE,
                (byte) 0xFF
        );

        sector.setData(raw);
    }

    // ============================================================
    //  HELPERS
    // ============================================================

    private static String readString(byte[] raw, int offset, int len) {
        return new String(raw, offset, len, StandardCharsets.US_ASCII).trim();
    }

    private static void writeString(byte[] raw, int offset, int len, String value) {
        byte[] bytes = value.getBytes(StandardCharsets.US_ASCII);
        int copyLen = Math.min(bytes.length, len);

        System.arraycopy(bytes, 0, raw, offset, copyLen);
        Arrays.fill(raw, offset + copyLen, offset + len, (byte) 0x20);
    }

    private static int readWord(byte[] raw, int offset) {
        return ((raw[offset] & 0xFF) << 8) | (raw[offset + 1] & 0xFF);
    }

    private static void writeWord(byte[] raw, int offset, int value) {
        raw[offset]     = (byte) ((value >> 8) & 0xFF);
        raw[offset + 1] = (byte) (value & 0xFF);
    }
}
