package com.miriki.ti99.dskimg.domain.fscheck;

import java.util.List;

/**
 * Represents the result of executing a FilesystemRepairPlan.
 * Contains information about what was repaired, what failed,
 * and any warnings or follow-up recommendations.
 */
public final class FilesystemRepairReport {

    private final List<String> successfulActions;
    private final List<String> failedActions;
    private final List<String> warnings;

    public FilesystemRepairReport(
            List<String> successfulActions,
            List<String> failedActions,
            List<String> warnings
    ) {
        this.successfulActions = List.copyOf(successfulActions);
        this.failedActions = List.copyOf(failedActions);
        this.warnings = List.copyOf(warnings);
    }

    public List<String> getSuccessfulActions() {
        return successfulActions;
    }

    public List<String> getFailedActions() {
        return failedActions;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public boolean isSuccessful() {
        return failedActions.isEmpty();
    }
}
