package com.miriki.ti99.dskimg.domain.constants;

/**
 * Canonical offsets and sizes for TI-99 disk structures
 * (HFDC / Myarc / CorComp compatible).
 *
 * This class serves as a central reference for all low-level
 * disk layout parameters and is intended for both documentation
 * and implementation use.
 */

public final class DiskLayoutConstants {

    private DiskLayoutConstants() {}

    public static final int SECTOR_SIZE = 256;
    
    public static final int ERASED_BYTE = 0x05;

    public static final int ZERO_BYTE = 0x00;

}
