package com.miriki.ti99.dskimg.domain.fscheck;

import java.util.List;

/**
 * Represents a high-level plan describing how a set of filesystem issues
 * could be repaired. This class contains no actual repair logic; it merely
 * describes what *should* be done.
 */
public final class FilesystemRepairPlan {

    private final List<FilesystemIssue> issues;
    private final List<String> actions;
    private final RepairPlanStatus status;

    public FilesystemRepairPlan(
            List<FilesystemIssue> issues,
            List<String> actions,
            RepairPlanStatus status
    ) {
        this.issues = List.copyOf(issues);
        this.actions = List.copyOf(actions);
        this.status = status;
    }

    public List<FilesystemIssue> getIssues() {
        return issues;
    }

    public List<String> getActions() {
        return actions;
    }

    public RepairPlanStatus getStatus() {
        return status;
    }

    public enum RepairPlanStatus {
        READY,
        NOTHING_TO_DO,
        UNSAFE,
        PARTIAL
    }
}
