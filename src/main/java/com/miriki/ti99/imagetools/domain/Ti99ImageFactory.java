package com.miriki.ti99.imagetools.domain;

import com.miriki.ti99.imagetools.domain.io.ImageFormatter;
import com.miriki.ti99.imagetools.io.Sector;
import com.miriki.ti99.imagetools.io.VolumeInformationBlockIO;

/**
 * High-level factory for creating new, empty TI-99 disk images.
 *
 * Responsibilities:
 *   - allocate the raw byte array
 *   - instantiate Ti99Image
 *   - run low-level formatting (ImageFormatter)
 *   - apply volume metadata (volume name)
 *
 * This class does NOT:
 *   - manipulate files
 *   - write directory entries
 *   - allocate clusters
 *   - modify the ABM beyond what ImageFormatter does
 *
 * All low-level work is delegated to ImageFormatter and the IO layer.
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
     * @param format     the disk format (geometry + layout)
     * @param volumeName optional volume name (null = default)
     * @return a fully initialized Ti99Image
     */
    public static Ti99Image createEmptyImage(DiskFormat format, String volumeName) {

        // ------------------------------------------------------------
        // 1. Allocate raw byte array
        // ------------------------------------------------------------
        int totalBytes = format.getTotalSectors() * Sector.SIZE;
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
            var vibSector = image.getSector(format.getVibSector());
            var vib = VolumeInformationBlockIO.readFrom(vibSector);

            vib.setVolumeName(volumeName);

            VolumeInformationBlockIO.writeTo(vibSector, vib);
        }

        return image;
    }
}
