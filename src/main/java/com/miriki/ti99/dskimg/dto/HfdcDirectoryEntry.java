package com.miriki.ti99.dskimg.dto;

public record HfdcDirectoryEntry(
        String fileName,
        int fdrSector,
        int totalSectors,
        int fileSizeBytes,
        String typeDescription,
        String formatDescription,
        String lengthDescription,
        boolean isProtected,
        boolean isFragmented,
        long creationTimestamp,
        long updateTimestamp
) {}
