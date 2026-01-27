package com.miriki.ti99.dskimg.domain.fscheck;

import java.util.List;

public record FilesystemCheckResult(
        FilesystemHealth health,
        List<FilesystemIssue> issues
) {}
