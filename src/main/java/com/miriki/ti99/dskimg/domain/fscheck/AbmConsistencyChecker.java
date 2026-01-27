package com.miriki.ti99.dskimg.domain.fscheck;

import java.util.ArrayList;
import java.util.List;

import com.miriki.ti99.dskimg.domain.Ti99FileSystem;

/**
 * Validates that the Allocation Bitmap (ABM) matches the actual
 * cluster usage derived from all file data chains.
 */
public final class AbmConsistencyChecker implements FilesystemChecker {

    @Override
    public FilesystemCheckResult check(Ti99FileSystem fs) {

        List<FilesystemIssue> issues = new ArrayList<>();

        // TODO: Implement ABM consistency validation
        // Example:
        // issues.add(new FilesystemIssue(
        //         FilesystemIssueType.ABM_INCONSISTENCY,
        //         "Cluster 50 is marked free in ABM but used by a file",
        //         Severity.ERROR,
        //         List.of(50)
        // ));

        FilesystemHealth health = issues.isEmpty()
                ? FilesystemHealth.GOOD
                : FilesystemHealth.WARN;

        return new FilesystemCheckResult(health, issues);
    }
}
