package com.miriki.ti99.imagetools.dto;

/**
 * Immutable DTO representing a single directory entry in a TI-99 volume.
 *
 * This is a high-level, UI-ready representation derived from:
 *   - File Descriptor Record (FDR)
 *   - Allocation Bitmap (ABM)
 *   - File type and flag decoding
 *   - Record length and format
 *   - Fragmentation analysis
 *
 * All fields are already formatted for display or further processing.
 */
public record Ti99DirectoryEntry(

        /** File name (already trimmed and normalized) */
        String name,

        /** Number of sectors allocated to the file */
        int usedSectors,

        /** Raw TI file type (e.g. "DISVAR", "PROGRAM", "INTFIX") */
        String rawType,

        /** Human-readable formatted type (e.g. "DIS/VAR 80") */
        String formattedType,

        /** Record format: "Fixed" or "Variable" */
        String recordFormat,

        /** Record length as formatted text (e.g. "80 bytes") */
        String recordLength,

        /** File size in bytes (calculated from record structure) */
        int fileSizeBytes,

        /** True if the file is protected (read-only) */
        boolean isProtected,

        /** True if the file is fragmented across non-contiguous clusters */
        boolean fragmented,

        /** 16-bit creation timestamp (HFDC format) */
        long createdTimestamp,

        /** 16-bit update timestamp (HFDC format) */
        long updatedTimestamp

) {}
