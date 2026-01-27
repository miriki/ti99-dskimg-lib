package com.miriki.ti99.dskimg.io;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.miriki.ti99.dskimg.domain.FileDescriptorRecord;
import com.miriki.ti99.dskimg.domain.constants.DiskLayoutConstants;
import com.miriki.ti99.dskimg.domain.constants.FdrConstants;

/**
 * Reads and writes a 256‑byte File Descriptor Record (FDR).
 *
 * This class performs raw sector‑level I/O only.
 */
public final class FileDescriptorRecordIO {

    private FileDescriptorRecordIO() {}

    // ============================================================
    //  PUBLIC API – READ
    // ============================================================

    public static FileDescriptorRecord readFrom(Sector sector) {
        Objects.requireNonNull(sector, "sector must not be null");

        byte[] raw = sector.getBytes();
        FileDescriptorRecord fdr = new FileDescriptorRecord();

        readHeaderFields(raw, fdr);
        readDataChainDcp(raw, fdr);

        return fdr;
    }

    // ============================================================
    //  PUBLIC API – WRITE
    // ============================================================

    public static void writeTo(Sector sector, FileDescriptorRecord fdr) {
        Objects.requireNonNull(sector, "sector must not be null");
        Objects.requireNonNull(fdr, "fdr must not be null");

        byte[] raw = new byte[DiskLayoutConstants.SECTOR_SIZE];

        writeHeaderFields(raw, fdr);
        writeDataChainDcp(raw, fdr);

        sector.setData(raw);
    }

    // ============================================================
    //  HEADER READ/WRITE
    // ============================================================

    private static void readHeaderFields(byte[] raw, FileDescriptorRecord fdr) {

        fdr.setFileName(readString(raw, FdrConstants.FDR_FILE_NAME, FdrConstants.FDR_FILE_NAME_SIZE));
        fdr.setExtendedRecordLength(readWord(raw, FdrConstants.FDR_EXTENDED_RECORD_LENGTH));
        fdr.setFileStatus(raw[FdrConstants.FDR_FILE_STATUS] & 0xFF);
        fdr.setRecordsPerSector(raw[FdrConstants.FDR_RECORDS_PER_SECTOR] & 0xFF);
        fdr.setTotalSectorsAllocated(readWord(raw, FdrConstants.FDR_TOTAL_SECTORS));
        fdr.setEofOffset(raw[FdrConstants.FDR_EOF_OFFSET] & 0xFF);
        fdr.setLogicalRecordLength(raw[FdrConstants.FDR_LOGICAL_RECORD_LENGTH] & 0xFF);
        fdr.setLevel3RecordsUsed(readWordLE(raw, FdrConstants.FDR_LEVEL3_RECORDS_USED));
        fdr.setCreationTimestamp(readDword(raw, FdrConstants.FDR_TIMESTAMP_CREATED));
        fdr.setUpdateTimestamp(readDword(raw, FdrConstants.FDR_TIMESTAMP_UPDATED));
    }

    private static void writeHeaderFields(byte[] raw, FileDescriptorRecord fdr) {

        writeString(raw, FdrConstants.FDR_FILE_NAME, FdrConstants.FDR_FILE_NAME_SIZE, fdr.getFileName());
        writeWord(raw, FdrConstants.FDR_EXTENDED_RECORD_LENGTH, fdr.getExtendedRecordLength());
        raw[FdrConstants.FDR_FILE_STATUS] = (byte) fdr.getFileStatus();
        raw[FdrConstants.FDR_RECORDS_PER_SECTOR] = (byte) fdr.getRecordsPerSector();
        writeWord(raw, FdrConstants.FDR_TOTAL_SECTORS, fdr.getTotalSectorsAllocated());
        raw[FdrConstants.FDR_EOF_OFFSET] = (byte) fdr.getEofOffset();
        raw[FdrConstants.FDR_LOGICAL_RECORD_LENGTH] = (byte) fdr.getLogicalRecordLength();
        writeWordLE(raw, FdrConstants.FDR_LEVEL3_RECORDS_USED, fdr.getLevel3RecordsUsed());
        writeDword(raw, FdrConstants.FDR_TIMESTAMP_CREATED, fdr.getCreationTimestamp());
        writeDword(raw, FdrConstants.FDR_TIMESTAMP_UPDATED, fdr.getUpdateTimestamp());
    }

    // ============================================================
    //  HFDC DCP READ/WRITE
    // ============================================================

    private static void readDataChainDcp(byte[] raw, FileDescriptorRecord fdr) {

        List<Integer> chain = new ArrayList<>();
        int off = FdrConstants.FDR_DCP_OFFSET;

        while (off + 2 < DiskLayoutConstants.SECTOR_SIZE) {

            int b0 = raw[off]     & 0xFF;
            int b1 = raw[off + 1] & 0xFF;
            int b2 = raw[off + 2] & 0xFF;

            if (b0 == 0 && b1 == 0 && b2 == 0) {
                break;
            }

            int N1 =  b0        & 0x0F;
            int N2 = (b0 >> 4)  & 0x0F;
            int N3 =  b1        & 0x0F;

            int M1 = (b1 >> 4)  & 0x0F;
            int M2 =  b2        & 0x0F;
            int M3 = (b2 >> 4)  & 0x0F;

            int start  = (N3 << 8) | (N2 << 4) | N1;
            int length = (M3 << 8) | (M2 << 4) | M1;

            for (int i = 0; i <= length; i++) {
                chain.add(start + i);
            }

            off += FdrConstants.DCP_ENTRY_SIZE;
        }

        fdr.setDataChain(chain);
    }

    private static void writeDataChainDcp(byte[] raw, FileDescriptorRecord fdr) {

        List<Integer> chain = fdr.getDataChain();
        int off = FdrConstants.FDR_DCP_OFFSET;

        if (chain == null || chain.isEmpty()) {
            raw[off] = raw[off + 1] = raw[off + 2] = 0;
            return;
        }

        int start = chain.get(0);
        int prev  = start;

        for (int i = 1; i <= chain.size(); i++) {

            boolean endOfBlock =
                    (i == chain.size()) ||
                    (chain.get(i) != prev + 1);

            if (endOfBlock) {

                int blockStart = start;
                int blockEnd   = prev;
                int length     = blockEnd - blockStart;

                int N1 =  blockStart        & 0x0F;
                int N2 = (blockStart >> 4)  & 0x0F;
                int N3 = (blockStart >> 8)  & 0x0F;

                int M1 =  length        & 0x0F;
                int M2 = (length >> 4)  & 0x0F;
                int M3 = (length >> 8)  & 0x0F;

                raw[off]     = (byte) ((N2 << 4) | N1);
                raw[off + 1] = (byte) ((M1 << 4) | N3);
                raw[off + 2] = (byte) ((M3 << 4) | M2);

                off += FdrConstants.DCP_ENTRY_SIZE;

                if (i < chain.size()) {
                    start = chain.get(i);
                }
            }

            if (i < chain.size()) {
                prev = chain.get(i);
            }
        }

        raw[off]     = 0;
        raw[off + 1] = 0;
        raw[off + 2] = 0;

        for (int i = off + 3; i < DiskLayoutConstants.SECTOR_SIZE; i++) {
            raw[i] = 0;
        }
    }

    // ============================================================
    //  LOW‑LEVEL HELPERS
    // ============================================================

    private static int readWord(byte[] raw, int off) {
        return ((raw[off] & 0xFF) << 8) | (raw[off + 1] & 0xFF);
    }

    private static int readWordLE(byte[] raw, int off) {
        return ((raw[off + 1] & 0xFF) << 8) | (raw[off] & 0xFF);
    }

    private static long readDword(byte[] raw, int off) {
        return ((long)(raw[off] & 0xFF) << 24)
             | ((long)(raw[off + 1] & 0xFF) << 16)
             | ((long)(raw[off + 2] & 0xFF) << 8)
             | ((long)(raw[off + 3] & 0xFF));
    }

    private static void writeWord(byte[] raw, int off, int value) {
        raw[off]     = (byte) ((value >> 8) & 0xFF);
        raw[off + 1] = (byte) (value & 0xFF);
    }

    private static void writeWordLE(byte[] raw, int off, int value) {
        raw[off]     = (byte) (value & 0xFF);
        raw[off + 1] = (byte) ((value >> 8) & 0xFF);
    }

    private static void writeDword(byte[] raw, int off, long value) {
        raw[off]     = (byte) ((value >> 24) & 0xFF);
        raw[off + 1] = (byte) ((value >> 16) & 0xFF);
        raw[off + 2] = (byte) ((value >> 8) & 0xFF);
        raw[off + 3] = (byte) (value & 0xFF);
    }

    private static String readString(byte[] raw, int off, int len) {
        byte[] buf = Arrays.copyOfRange(raw, off, off + len);
        return new String(buf, StandardCharsets.US_ASCII).trim();
    }

    private static void writeString(byte[] raw, int off, int len, String s) {
        byte[] buf = new byte[len];
        Arrays.fill(buf, (byte) 0x20);
        byte[] src = s.getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(src, 0, buf, 0, Math.min(src.length, len));
        System.arraycopy(buf, 0, raw, off, len);
    }
}
