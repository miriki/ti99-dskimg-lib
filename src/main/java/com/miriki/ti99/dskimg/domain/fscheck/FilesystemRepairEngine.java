package com.miriki.ti99.dskimg.domain.fscheck;

import java.util.ArrayList;
import java.util.List;

import com.miriki.ti99.dskimg.domain.Ti99FileSystem;

public final class FilesystemRepairEngine {

    private final List<FilesystemRepairStrategy> strategies = new ArrayList<>();

    public void register(FilesystemRepairStrategy strategy) {
        strategies.add(strategy);
    }

    public void repair(Ti99FileSystem fs, List<FilesystemIssue> issues) {
        for (FilesystemIssue issue : issues) {
            for (FilesystemRepairStrategy s : strategies) {
                s.repair(fs, issue);
            }
        }
    }
}
