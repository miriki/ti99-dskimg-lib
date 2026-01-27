package com.miriki.ti99.dskimg.domain.fscheck;

import java.util.ArrayList;
import java.util.List;

import com.miriki.ti99.dskimg.domain.Ti99FileSystem;

/**
 * Checks the directory structure for:
 *   - gaps in the FDI
 *   - invalid FDR references
 *   - missing or duplicate FDR sectors
 *   - mismatches between FDI and actual file list
 */
public final class DirectoryConsistencyChecker implements FilesystemChecker {

    @Override
    public FilesystemCheckResult check(Ti99FileSystem fs) {

        List<FilesystemIssue> issues = new ArrayList<>();

        // TODO: Implement directory consistency checks
        // Example:
        // issues.add(new FilesystemIssue(
        //         FilesystemIssueType.INVALID_FDI_ENTRY,
        //         "FDI entry 12 references sector 0 (unused)",
        //         Severity.WARNING,
        //         List.of(12)
        // ));

        FilesystemHealth health = issues.isEmpty()
                ? FilesystemHealth.GOOD
                : FilesystemHealth.WARN;

        return new FilesystemCheckResult(health, issues);
    }
}
