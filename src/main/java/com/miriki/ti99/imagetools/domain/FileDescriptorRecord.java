package com.miriki.ti99.imagetools.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a TI-99 HFDC / Level-3 File Descriptor Record (FDR).
 *
 * This class models the *exact* on-disk layout of the FDR (bytes 0x00–0x1B),
 * plus additional convenience fields that are computed by higher-level
 * components (DirectoryReader, DataChainReader, etc.).
 *
 * Layout summary (HFDC Level-3):
 *   00..09  File name (10 chars)
 *   0A..0B  Extended record length (WORD BE)
 *   0C      File status byte (type + flags)
 *   0D      Records per sector
 *   0E..0F  Total sectors allocated (WORD BE)
 *   10      EOF offset (0–255)
 *   11      Logical record length
 *   12..13  Level-3 records used (WORD LE)
 *   14..17  Creation timestamp (DWORD BE)
 *   18..1B  Update timestamp   (DWORD BE)
 *
 * Convenience fields (not stored in FDR):
 *   - first/last sector
 *   - used sectors / clusters
 *   - next FDR pointer
 *   - data chain (list of sector numbers)
 */
public final class FileDescriptorRecord {

    // ============================================================
    //  HFDC / LEVEL-3 FDR FIELDS (EXACT ON-DISK LAYOUT)
    // ============================================================

    private String fileName = "";            // 00..09 (10 chars)
    private int extendedRecordLength = 0;    // 0A..0B (WORD BE)
    private int fileStatus = 0;              // 0C (BYTE)
    private int recordsPerSector = 0;        // 0D (BYTE)
    private int totalSectorsAllocated = 0;   // 0E..0F (WORD BE)
    private int eofOffset = 0;               // 10 (BYTE)
    private int logicalRecordLength = 0;     // 11 (BYTE)
    private int level3RecordsUsed = 0;       // 12..13 (WORD LE)
    private long creationTimestamp = 0;      // 14..17 (DWORD BE)
    private long updateTimestamp = 0;        // 18..1B (DWORD BE)

    // ============================================================
    //  CONVENIENCE FIELDS (NOT STORED IN FDR)
    // ============================================================

    private int firstSector = 0;
    private int lastSector = 0;
    private int usedSectors = 0;
    private int usedClusters = 0;
    private int nextFdr = 0;
    private List<Integer> dataChain = new ArrayList<>();

    // ============================================================
    //  CONSTANTS – TYPE/FLAG MASKS
    // ============================================================

    public static final int TYPE_MASK = 0x0F;
    public static final int FLAG_MASK = 0xF0;

    // ============================================================
    //  BASIC STATE
    // ============================================================

    /**
     * Returns true if this FDR is logically empty.
     */
    public boolean isEmpty() {
        return (fileName == null || fileName.isBlank())
                && totalSectorsAllocated == 0
                && dataChain.isEmpty();
    }

    // ============================================================
    //  GETTERS – EXACT FDR FIELDS
    // ============================================================

    public String getFileName() { return fileName; }
    public int getExtendedRecordLength() { return extendedRecordLength; }
    public int getFileStatus() { return fileStatus; }
    public int getRecordsPerSector() { return recordsPerSector; }
    public int getTotalSectorsAllocated() { return totalSectorsAllocated; }
    public int getEofOffset() { return eofOffset; }
    public int getLogicalRecordLength() { return logicalRecordLength; }
    public int getRecordLength() { return logicalRecordLength; }
    public int getLevel3RecordsUsed() { return level3RecordsUsed; }
    public long getCreationTimestamp() { return creationTimestamp; }
    public long getUpdateTimestamp() { return updateTimestamp; }
    public long getUpdateDate() { return updateTimestamp; }

    // ============================================================
    //  GETTERS – CONVENIENCE FIELDS
    // ============================================================

    public int getFirstSector() { return firstSector; }
    public int getLastSector() { return lastSector; }
    public int getUsedSectors() { return usedSectors; }
    public int getUsedClusters() { return usedClusters; }
    public int getNextFdr() { return nextFdr; }
    public List<Integer> getDataChain() { return dataChain; }

    /**
     * Computes the logical file size in bytes.
     */
    public int getFileSizeInBytes() {
        return usedSectors <= 0 ? 0 : (usedSectors - 1) * 256 + eofOffset;
    }

    /**
     * Returns true if the data chain is not strictly sequential.
     */
    public boolean isFragmented() {
        for (int i = 1; i < dataChain.size(); i++) {
            if (dataChain.get(i) != dataChain.get(i - 1) + 1) {
                return true;
            }
        }
        return false;
    }

    // ============================================================
    //  FLAG HELPERS
    // ============================================================

    public boolean hasFlag(FileFlagBits flag) { return (fileStatus & flag.mask) != 0; }
    public boolean isProtected() { return hasFlag(FileFlagBits.PROTECTED); }
    public boolean isBackup() { return hasFlag(FileFlagBits.BACKUP); }
    public boolean isEmulate() { return hasFlag(FileFlagBits.EMULATE); }

    // ============================================================
    //  SETTERS – EXACT FDR FIELDS
    // ============================================================

    public void setFileName(String v) { fileName = (v == null ? "" : v); }
    public void setExtendedRecordLength(int v) { extendedRecordLength = v & 0xFFFF; }
    public void setFileStatus(int v) { fileStatus = v & 0xFF; }
    public void setRecordsPerSector(int v) { recordsPerSector = v & 0xFF; }
    public void setTotalSectorsAllocated(int v) { totalSectorsAllocated = v & 0xFFFF; }
    public void setEofOffset(int v) { eofOffset = v & 0xFF; }
    public void setLogicalRecordLength(int v) { logicalRecordLength = v & 0xFF; }
    public void setLevel3RecordsUsed(int v) { level3RecordsUsed = v & 0xFFFF; }
    public void setCreationTimestamp(long v) { creationTimestamp = v; }
    public void setUpdateTimestamp(long v) { updateTimestamp = v; }

    // ============================================================
    //  SETTERS – CONVENIENCE FIELDS
    // ============================================================

    public void setFirstSector(int v) { firstSector = v & 0xFFFF; }
    public void setLastSector(int v) { lastSector = v & 0xFFFF; }
    public void setUsedSectors(int v) { usedSectors = v & 0xFFFF; }
    public void setUsedClusters(int v) { usedClusters = v & 0xFFFF; }
    public void setNextFdr(int v) { nextFdr = v & 0xFFFF; }
    public void setDataChain(List<Integer> chain) { dataChain = new ArrayList<>(chain); }
    public void addDataChainEntry(int sector) { dataChain.add(sector & 0xFFFF); }

    // ============================================================
    //  TYPE + FLAG HELPERS (compatible with FileFlagsIO)
    // ============================================================

    public int getFileType() { return fileStatus & TYPE_MASK; }

    public void setFileType(int type) {
        fileStatus = (fileStatus & FLAG_MASK) | (type & TYPE_MASK);
    }

    public int getFlags() {
        return (fileStatus & FLAG_MASK) >>> 4;
    }

    public void setFlags(int flags) {
        fileStatus = (fileStatus & TYPE_MASK) | ((flags << 4) & FLAG_MASK);
    }

    // ============================================================
    //  FLUENT MODIFIERS
    // ============================================================

    public FileDescriptorRecord withFlags(int v) { setFlags(v); return this; }
    public FileDescriptorRecord withUsedSectors(int v) { setUsedSectors(v); return this; }
    public FileDescriptorRecord withEofOffset(int v) { setEofOffset(v); return this; }
    public FileDescriptorRecord withUpdateDate(long v) { setUpdateTimestamp(v); return this; }
    public FileDescriptorRecord withDataChain(List<Integer> v) { setDataChain(v); return this; }
    public FileDescriptorRecord withRecordLength(int v) { setLogicalRecordLength(v); return this; }
}
