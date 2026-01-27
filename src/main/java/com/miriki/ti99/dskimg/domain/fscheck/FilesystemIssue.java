package com.miriki.ti99.dskimg.domain.fscheck;

import java.util.List;

public record FilesystemIssue(
        FilesystemIssueType type,
        String message,
        Severity severity,
        List<Integer> affectedSectors
) {}
