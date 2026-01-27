package com.miriki.ti99.dskimg.domain.fscheck;

import com.miriki.ti99.dskimg.domain.Ti99FileSystem;

public interface FilesystemChecker {

    FilesystemCheckResult check(Ti99FileSystem fs);
}
