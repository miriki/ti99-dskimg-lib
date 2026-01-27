package com.miriki.ti99.dskimg.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import com.miriki.ti99.dskimg.domain.constants.DiskLayoutConstants;
import com.miriki.ti99.dskimg.domain.enums.FileType;
import com.miriki.ti99.dskimg.domain.enums.RecordFormat;

/**
 * Represents a TI-99 HFDC / Level-3 File Descriptor Record (FDR).
 *
 * This class models the exact on-disk layout of the FDR (bytes 0x00–0x1B),
 * plus additional convenience fields computed by higher-level components.
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

    private String fileName = "";
    private int extendedRecordLength = 0;
    private int fileStatus = 0;
    private int recordsPerSector = 0;
    private int totalSectorsAllocated = 0;
    private int eofOffset = 0;
    private int logicalRecordLength = 0;
    private int level3RecordsUsed = 0;
    private long creationTimestamp = 0;
    private long updateTimestamp = 0;

    // ============================================================
    //  CONVENIENCE FIELDS (NOT STORED IN FDR)
    // ============================================================

    private int firstSector = 0;
    private int lastSector = 0;
    private int usedSectors = 0;
    private int usedClusters = 0;
    private int nextFdr = 0;

    /** Mutable internally, but exposed immutably. */
    private List<Integer> dataChain = new ArrayList<>();


    // ============================================================
    //  BASIC STATE
    // ============================================================

    /**
     * Returns true if this FDR is logically empty.
     * HFDC considers an FDR empty if the filename starts with 0x00.
     */
    public boolean isEmpty() {
        return fileName == null || fileName.isBlank();
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
    public int getLevel3RecordsUsed() { return level3RecordsUsed; }
    public long getCreationTimestamp() { return creationTimestamp; }
    public long getUpdateTimestamp() { return updateTimestamp; }


    // ============================================================
    //  GETTERS – CONVENIENCE FIELDS
    // ============================================================

    public int getFirstSector() { return firstSector; }
    public int getLastSector() { return lastSector; }
    public int getUsedSectors() { return usedSectors; }
    public int getUsedClusters() { return usedClusters; }
    public int getNextFdr() { return nextFdr; }

    /** Returns an immutable view of the data chain. */
    public List<Integer> getDataChain() {
        return Collections.unmodifiableList(dataChain);
    }

    /** Computes the logical file size in bytes. */
    public int getFileSizeInBytes() {
        if (usedSectors <= 0) return 0;
        return (usedSectors - 1) * DiskLayoutConstants.SECTOR_SIZE + eofOffset;
    }

    /** Returns true if the data chain is not strictly sequential. */
    public boolean isFragmented() {
        for (int i = 1; i < dataChain.size(); i++) {
            if (dataChain.get(i) != dataChain.get(i - 1) + 1) {
                return true;
            }
        }
        return false;
    }


    // ============================================================
    //  STATUS BYTE HANDLING (via FdrStatus)
    // ============================================================

    public FdrStatus getStatus() {
        return FdrStatus.fromByte(fileStatus);
    }

    public void setStatus(FdrStatus status) {
        this.fileStatus = status.toByte();
    }

    public FileType getFileType() {
        return getStatus().getType();
    }

    public RecordFormat getRecordFormat() {
        return getStatus().getFormat();
    }

    public EnumSet<FileAttribute> getAttributes() {
        return getStatus().getAttributes();
    }


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

    public void setDataChain(List<Integer> chain) {
        dataChain = new ArrayList<>(chain);
    }

    public void addDataChainEntry(int sector) {
        dataChain.add(sector & 0xFFFF);
    }


    // ============================================================
    //  FLUENT MODIFIERS (mutating, but convenient)
    // ============================================================

    public FileDescriptorRecord withUsedSectors(int v) { setUsedSectors(v); return this; }
    public FileDescriptorRecord withEofOffset(int v) { setEofOffset(v); return this; }
    public FileDescriptorRecord withUpdateDate(long v) { setUpdateTimestamp(v); return this; }
    public FileDescriptorRecord withDataChain(List<Integer> v) { setDataChain(v); return this; }
    public FileDescriptorRecord withRecordLength(int v) { setLogicalRecordLength(v); return this; }

    public FileDescriptorRecord withStatus(FdrStatus status) {
        setStatus(status);
        return this;
    }

    public FileDescriptorRecord withFileStatus(int status) {
        setFileStatus(status);
        return this;
    }
}
