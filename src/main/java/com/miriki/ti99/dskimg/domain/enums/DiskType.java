package com.miriki.ti99.dskimg.domain.enums;

public enum DiskType {
    // SSSD / DSSD / DSDD / SSSD‑HFDC / Myarc‑DSK
    // automatische Erkennung aus VIB
    // Mapping zu DiskDensity + DiskSides + Tracks
    SSSD,
    DSSD,
    DSDD,
    HFDC_SSSD,
    MYARC_DSK
}

// später:
/*
public static DiskType detectFromVib(Vib vib) {
    // TODO: implement
}
*/
