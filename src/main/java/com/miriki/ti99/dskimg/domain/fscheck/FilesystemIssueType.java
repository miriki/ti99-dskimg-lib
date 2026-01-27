package com.miriki.ti99.dskimg.domain.fscheck;

public enum FilesystemIssueType {
    ORPHAN_CLUSTER,
    CROSS_LINK,
    INVALID_FDR,
    INVALID_FDI_ENTRY,
    ABM_INCONSISTENCY,
    FRAGMENTATION,
    INVALID_SECTOR_REFERENCE,
    DIRECTORY_GAP,
    UNKNOWN
}
