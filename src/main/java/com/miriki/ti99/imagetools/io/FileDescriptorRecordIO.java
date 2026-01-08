package com.miriki.ti99.imagetools.io;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.miriki.ti99.imagetools.domain.FileDescriptorRecord;

/**
 * IO helper for reading and writing a 256-byte File Descriptor Record (FDR).
 *
 * The FDR contains:
 *   - file name
 *   - record structure (fixed/variable)
 *   - record length
 *   - sector allocation
 *   - timestamps
 *   - legacy data chain (WORD list)
 *   - HFDC Data Chain Pointer (DCP) block
 *
 * This class performs raw sector-level I/O only. Higher-level interpretation
 * is handled by the domain layer.
 */
public final class FileDescriptorRecordIO {

    private static final Logger log = LoggerFactory.getLogger(FileDescriptorRecordIO.class);

    private static final int SECTOR_SIZE = 256;

    // ============================================================
    //  FDR LAYOUT OFFSETS
    // ============================================================

    private static final int OFF_FILENAME             = 0x00; // 10 bytes
    private static final int OFF_EXT_RECORD_LENGTH    = 0x0A; // WORD BE
    private static final int OFF_FILE_STATUS          = 0x0C; // BYTE
    private static final int OFF_RECORDS_PER_SECTOR   = 0x0D; // BYTE
    private static final int OFF_TOTAL_SECTORS_ALLOC  = 0x0E; // WORD BE
    private static final int OFF_EOF_OFFSET           = 0x10; // BYTE
    private static final int OFF_LOGICAL_REC_LENGTH   = 0x11; // BYTE
    private static final int OFF_LEVEL3_RECORDS_USED  = 0x12; // WORD LE (!)
    private static final int OFF_CREATION_TIMESTAMP   = 0x14; // DWORD BE
    private static final int OFF_UPDATE_TIMESTAMP     = 0x18; // DWORD BE
    private static final int OFF_DATA_CHAIN           = 0x1C; // WORD list (legacy)

    private static final int FILENAME_LENGTH          = 10;

    private FileDescriptorRecordIO() {
        // Utility class – no instances
    }

    // ============================================================
    //  PUBLIC API – READ
    // ============================================================

    /**
     * Reads a File Descriptor Record (FDR) from the given sector.
     */
    public static FileDescriptorRecord readFrom(Sector sector) {
        Objects.requireNonNull(sector, "sector must not be null");
        log.debug("readFrom({})", sector);

        byte[] raw = sector.getBytes();
        FileDescriptorRecord fdr = new FileDescriptorRecord();

        // readHeaderFields(raw, fdr);
        // readDataChain(raw, fdr);
        readHeaderFields(raw, fdr);
        readDataChainDcp(raw, fdr);

        log.info("  FDR.readFrom({}) --> {}", sector, fdr);
        return fdr;
    }

    // ============================================================
    //  PUBLIC API – WRITE
    // ============================================================

    /**
     * Writes a File Descriptor Record (FDR) into the given sector.
     */
    public static void writeTo(Sector sector, FileDescriptorRecord fdr) {
        Objects.requireNonNull(sector, "sector must not be null");
        Objects.requireNonNull(fdr, "FileDescriptorRecord must not be null");

        log.debug("writeTo({}, {})", sector, fdr);

        byte[] raw = new byte[SECTOR_SIZE];

        writeHeaderFields(raw, fdr);
        writeDataChainDcp(raw, fdr);

        // Write buffer back to sector
        for (int i = 0; i < SECTOR_SIZE; i++) {
            sector.set(i, raw[i]);
        }

        log.info("  FDR.writeTo({}, {})", sector, fdr);
        logHexDump(raw);
    }

    // ============================================================
    //  INTERNAL – HEADER READ/WRITE
    // ============================================================

    private static void readHeaderFields(byte[] raw, FileDescriptorRecord fdr) {

        // 00..09: filename
        fdr.setFileName(readString(raw, OFF_FILENAME, FILENAME_LENGTH));

        // 0A..0B: extended record length (WORD, BE)
        fdr.setExtendedRecordLength(readWord(raw, OFF_EXT_RECORD_LENGTH));

        // 0C: file status (BYTE)
        fdr.setFileStatus(raw[OFF_FILE_STATUS] & 0xFF);

        // 0D: records per sector (BYTE)
        fdr.setRecordsPerSector(raw[OFF_RECORDS_PER_SECTOR] & 0xFF);

        // 0E..0F: total sectors allocated (WORD, BE)
        fdr.setTotalSectorsAllocated(readWord(raw, OFF_TOTAL_SECTORS_ALLOC));

        // 10: EOF offset (BYTE)
        fdr.setEofOffset(raw[OFF_EOF_OFFSET] & 0xFF);

        // 11: logical record length (BYTE)
        fdr.setLogicalRecordLength(raw[OFF_LOGICAL_REC_LENGTH] & 0xFF);

        // 12..13: level 3 records used (WORD, LITTLE endian!)
        fdr.setLevel3RecordsUsed(readWordLE(raw, OFF_LEVEL3_RECORDS_USED));

        // 14..17: created timestamp (DWORD, BE)
        fdr.setCreationTimestamp(readDword(raw, OFF_CREATION_TIMESTAMP));

        // 18..1B: updated timestamp (DWORD, BE)
        fdr.setUpdateTimestamp(readDword(raw, OFF_UPDATE_TIMESTAMP));
    }

    private static void writeHeaderFields(byte[] raw, FileDescriptorRecord fdr) {

        writeString(raw, OFF_FILENAME, FILENAME_LENGTH, fdr.getFileName());
        writeWord(raw, OFF_EXT_RECORD_LENGTH, fdr.getExtendedRecordLength());
        raw[OFF_FILE_STATUS] = (byte) fdr.getFileStatus();
        raw[OFF_RECORDS_PER_SECTOR] = (byte) fdr.getRecordsPerSector();
        writeWord(raw, OFF_TOTAL_SECTORS_ALLOC, fdr.getTotalSectorsAllocated());
        raw[OFF_EOF_OFFSET] = (byte) fdr.getEofOffset();
        raw[OFF_LOGICAL_REC_LENGTH] = (byte) fdr.getLogicalRecordLength();
        writeWordLE(raw, OFF_LEVEL3_RECORDS_USED, fdr.getLevel3RecordsUsed());
        writeDword(raw, OFF_CREATION_TIMESTAMP, fdr.getCreationTimestamp());
        writeDword(raw, OFF_UPDATE_TIMESTAMP, fdr.getUpdateTimestamp());
    }

    // ============================================================
    //  INTERNAL – HFDC DCP READ (CONTIGUOUS RANGE)
    // ============================================================

    /**
     * Reads TI-DSR 3-byte DCP entries from the FDR.
     * Each entry is 3 bytes = 6 nibbles:
     *
     *   Byte0: N2 N1
     *   Byte1: M1 N3
     *   Byte2: M3 M2
     *
     * Start  = 0x0 N3 N2 N1
     * Length = 0x0 M3 M2 M1
     *
     * Terminator = 00 00 00
     */
    private static void readDataChainDcp(byte[] raw, FileDescriptorRecord fdr) {

        List<Integer> chain = new ArrayList<>();
        int off = OFF_DATA_CHAIN;

        while (off + 2 < SECTOR_SIZE) {

            int b0 = raw[off]     & 0xFF;
            int b1 = raw[off + 1] & 0xFF;
            int b2 = raw[off + 2] & 0xFF;

            // Terminator?
            if (b0 == 0 && b1 == 0 && b2 == 0) {
                break;
            }

            // Nibbles
            int N1 =  b0        & 0x0F;
            int N2 = (b0 >> 4)  & 0x0F;
            int N3 =  b1        & 0x0F;

            int M1 = (b1 >> 4)  & 0x0F;
            int M2 =  b2        & 0x0F;
            int M3 = (b2 >> 4)  & 0x0F;

            int start  = (N3 << 8) | (N2 << 4) | N1;
            int length = (M3 << 8) | (M2 << 4) | M1;

            // 1 Cluster = 1 Sektor → expand to full chain
            for (int i = 0; i <= length; i++) {
                chain.add(start + i);
            }

            off += 3;
        }

        fdr.setDataChain(chain);
    }

    // ============================================================
    //  INTERNAL – HFDC DCP WRITE (CONTIGUOUS RANGE)
    // ============================================================

    /**
     * Writes TI-DSR 3-byte DCP entries into the FDR.
     * Each entry is 3 bytes = 6 nibbles:
     *
     *   Byte0: N2 N1
     *   Byte1: M1 N3
     *   Byte2: M3 M2
     *
     * Start  = 0x0 N3 N2 N1
     * Length = 0x0 M3 M2 M1
     *
     * Terminator = 00 00 00
     */
    private static void writeDataChainDcp(byte[] raw, FileDescriptorRecord fdr) {

        List<Integer> chain = fdr.getDataChain();
        int off = OFF_DATA_CHAIN;

        if (chain == null || chain.isEmpty()) {
            raw[off] = raw[off + 1] = raw[off + 2] = 0;
            return;
        }

        // Wir unterstützen Fragmentierung:
        // Kette in zusammenhängende Blöcke zerlegen
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

                // Nibbles für Start
                int N1 =  blockStart        & 0x0F;
                int N2 = (blockStart >> 4)  & 0x0F;
                int N3 = (blockStart >> 8)  & 0x0F;

                // Nibbles für Länge
                int M1 =  length        & 0x0F;
                int M2 = (length >> 4)  & 0x0F;
                int M3 = (length >> 8)  & 0x0F;

                raw[off]     = (byte) ((N2 << 4) | N1);
                raw[off + 1] = (byte) ((M1 << 4) | N3);
                raw[off + 2] = (byte) ((M3 << 4) | M2);

                off += 3;

                // Nächster Block beginnt hier
                if (i < chain.size()) {
                    start = chain.get(i);
                }
            }

            if (i < chain.size()) {
                prev = chain.get(i);
            }
        }

        // Terminator
        raw[off]     = 0;
        raw[off + 1] = 0;
        raw[off + 2] = 0;

        // Rest nullen
        for (int i = off + 3; i < SECTOR_SIZE; i++) {
            raw[i] = 0;
        }
    }

    // ============================================================
    //  LOW-LEVEL HELPERS
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

    private static void logHexDump(byte[] raw) {
        StringBuilder sb = new StringBuilder(SECTOR_SIZE * 3);
        for (int i = 0; i < SECTOR_SIZE; i++) {
            sb.append(String.format("%02X ", raw[i] & 0xFF));
        }
        log.info("  data: {}", sb.toString());
    }
}
