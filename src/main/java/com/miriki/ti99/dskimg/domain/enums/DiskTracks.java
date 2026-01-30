package com.miriki.ti99.dskimg.domain.enums;

public enum DiskTracks {
    T40(40),
    T80(80);

    private final int tracks;

    DiskTracks(int tracks) {
        this.tracks = tracks;
    }

    public int getTracks() {
        return tracks;
    }
}
