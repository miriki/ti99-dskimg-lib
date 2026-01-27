package com.miriki.ti99.dskimg.domain.fscheck;

import java.util.ArrayList;
import java.util.List;

import com.miriki.ti99.dskimg.domain.Ti99FileSystem;

/**
 * Detects clusters that appear in more than one file's data chain.
 * Cross-linked clusters indicate severe filesystem corruption.
 */
public final class CrossLinkChecker implements FilesystemChecker {

    @Override
    public FilesystemCheckResult check(Ti99FileSystem fs) {

        List<FilesystemIssue> issues = new ArrayList<>();

        // TODO: Implement cross-link detection
        // Example:
        // issues.add(new FilesystemIssue(
        //         FilesystemIssueType.CROSS_LINK,
        //         "Cluster 200 is referenced by multiple files",
        //         Severity.CRITICAL,
        //         List.of(200)
        // ));

        FilesystemHealth health = issues.isEmpty()
                ? FilesystemHealth.GOOD
                : FilesystemHealth.BROKEN;

        return new FilesystemCheckResult(health, issues);
    }
}
