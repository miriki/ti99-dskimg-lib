package com.miriki.ti99.dskimg.domain;

import java.util.Objects;

import com.miriki.ti99.dskimg.domain.constants.DiskLayoutConstants;
import com.miriki.ti99.dskimg.domain.io.ImageFormatter;
import com.miriki.ti99.dskimg.io.VolumeInformationBlockIO;

/**
 * High-level factory for creating new, empty TI-99 disk images.
 *
 * Responsibilities:
 *   - allocate the raw byte array
 *   - instantiate Ti99Image
 *   - run low-level formatting (ImageFormatter)
 *   - apply volume metadata (volume name)
 *
 * No file manipulation or directory logic is performed here.
 */
public final class Ti99ImageFactory {

    private Ti99ImageFactory() {
        // utility class – no instances
    }

    // ============================================================
    //  PUBLIC API
    // ============================================================

    /**
     * Creates a new, empty TI-99 disk image using the given DiskFormat.
     * The image is fully formatted (VIB, ABM, FDI) and ready for use.
     *
     * @param format     disk geometry and layout
     * @param volumeName optional volume name (null or blank = default)
     */
    public static Ti99Image createEmptyImage(DiskFormat format, String volumeName) {

        Objects.requireNonNull(format, "format must not be null");

        // ------------------------------------------------------------
        // 1. Allocate raw byte array
        // ------------------------------------------------------------
        int totalBytes = format.getTotalSectors() * DiskLayoutConstants.SECTOR_SIZE;
        byte[] data = new byte[totalBytes];

        // ------------------------------------------------------------
        // 2. Create image wrapper
        // ------------------------------------------------------------
        Ti99Image image = new Ti99Image(format, data);

        // ------------------------------------------------------------
        // 3. Low-level formatting (clear sectors, write VIB, ABM, FDI)
        // ------------------------------------------------------------
        ImageFormatter.initialize(image);

        // ------------------------------------------------------------
        // 4. Apply volume name (optional)
        // ------------------------------------------------------------
        if (volumeName != null && !volumeName.isBlank()) {

            String normalizedName = volumeName.trim();

            var vibSector = image.getSector(format.getVibSector());
            var vib = VolumeInformationBlockIO.readFrom(vibSector);

            vib.setVolumeName(normalizedName);

            VolumeInformationBlockIO.writeTo(vibSector, vib);
        }

        return image;
    }
}
