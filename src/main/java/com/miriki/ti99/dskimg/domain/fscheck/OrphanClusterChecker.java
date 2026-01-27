package com.miriki.ti99.dskimg.domain.fscheck;

import java.util.ArrayList;
import java.util.List;

import com.miriki.ti99.dskimg.domain.Ti99FileSystem;

/**
 * Detects clusters that are marked as used in the ABM but do not belong
 * to any file's data chain.
 */
public final class OrphanClusterChecker implements FilesystemChecker {

    @Override
    public FilesystemCheckResult check(Ti99FileSystem fs) {

        List<FilesystemIssue> issues = new ArrayList<>();

        // TODO: Implement orphan cluster detection
        // Example:
        // issues.add(new FilesystemIssue(
        //         FilesystemIssueType.ORPHAN_CLUSTER,
        //         "Cluster 123 is marked as used but not referenced by any file",
        //         Severity.WARNING,
        //         List.of(123)
        // ));

        FilesystemHealth health = issues.isEmpty()
                ? FilesystemHealth.GOOD
                : FilesystemHealth.WARN;

        return new FilesystemCheckResult(health, issues);
    }
}
