package com.miriki.ti99.imagetools.domain.io;

import com.miriki.ti99.imagetools.domain.*;
import com.miriki.ti99.imagetools.io.AllocationBitmapIO;
import com.miriki.ti99.imagetools.io.FileDescriptorIndexIO;
import com.miriki.ti99.imagetools.io.FileDescriptorRecordIO;
import com.miriki.ti99.imagetools.io.Sector;
import com.miriki.ti99.imagetools.io.VolumeInformationBlockIO;

public final class Ti99FileSystemIO {

    private Ti99FileSystemIO() {}

    // ============================================================
    //  READ FILESYSTEM
    // ============================================================

    public static Ti99FileSystem read(Ti99Image image) {

        Ti99FileSystem fs = new Ti99FileSystem(image);

        // --- VIB ---
        Sector vibSector = image.getSector(0);
        VolumeInformationBlock vib = VolumeInformationBlockIO.readFrom(vibSector);
        fs.setVib(vib);

        // --- ABM ---
        int totalSectors = vib.getTotalSectors();
        fs.setAbm(AllocationBitmapIO.read(vibSector, totalSectors));

        // --- FDI ---
        Sector fdiSector = image.getSector(1);
        FileDescriptorIndex fdi = FileDescriptorIndexIO.readFrom(fdiSector);
        fs.setFdi(fdi);

        // --- FDRs ---
        int[] fdrSectors = fdi.getFdrSectors();

        for (int sectorIndex : fdrSectors) {

            if (sectorIndex == 0) continue; // leerer Eintrag

            Sector s = image.getSector(sectorIndex);
            FileDescriptorRecord fdr = FileDescriptorRecordIO.readFrom(s);

            if (!fdr.isEmpty()) {
                fs.addFile(fdr);
            }
        }

        return fs;
    }

    // ============================================================
    //  WRITE FILESYSTEM
    // ============================================================

    public static void write(Ti99FileSystem fs) {

        Ti99Image image = fs.getImage();

        // --- VIB ---
        Sector vibSector = image.getSector(0);
        VolumeInformationBlockIO.writeTo(vibSector, fs.getVib());

        // --- ABM ---
        AllocationBitmapIO.write(vibSector, fs.getAbm());

        // --- FDI ---
        Sector fdiSector = image.getSector(1);
        FileDescriptorIndexIO.writeTo(fdiSector, fs.getFdi());

        // --- FDRs ---
        int[] fdrSectors = fs.getFdi().getFdrSectors();

        for (int i = 0; i < fdrSectors.length; i++) {

            int sectorIndex = fdrSectors[i];
            if (sectorIndex == 0) continue; // kein FDR

            FileDescriptorRecord fdr = fs.getFiles().get(i);

            Sector s = image.getSector(sectorIndex);
            FileDescriptorRecordIO.writeTo(s, fdr);
        }
    }
}
