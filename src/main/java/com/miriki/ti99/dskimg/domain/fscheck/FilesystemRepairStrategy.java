package com.miriki.ti99.dskimg.domain.fscheck;

import com.miriki.ti99.dskimg.domain.Ti99FileSystem;

public interface FilesystemRepairStrategy {

    void repair(Ti99FileSystem fs, FilesystemIssue issue);
}
