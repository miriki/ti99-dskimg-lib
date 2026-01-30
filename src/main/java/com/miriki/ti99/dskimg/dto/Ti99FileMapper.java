package com.miriki.ti99.dskimg.dto;

import com.miriki.ti99.dskimg.domain.Ti99File;

public final class Ti99FileMapper {

    private Ti99FileMapper() {}

    public static Ti99FileDTO toDTO(Ti99File file) {

        return new Ti99FileDTO(
                file.getFileName(),
                file.getType().name(),        // oder file.getType().toString()
                file.getRecordLength(),
                file.getContent(),            // content ist im DTO immutable kopiert
                file.getFlags(),
                file.getRecordsPerSector(),
                file.getTimestamp()
        );
    }
}
