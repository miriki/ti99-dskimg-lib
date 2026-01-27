package com.miriki.ti99.dskimg.domain.io;

import com.miriki.ti99.dskimg.domain.*;
import com.miriki.ti99.dskimg.domain.constants.FdiConstants;
import com.miriki.ti99.dskimg.domain.constants.VibConstants;
import com.miriki.ti99.dskimg.io.AllocationBitmapIO;
import com.miriki.ti99.dskimg.io.FileDescriptorIndexIO;
import com.miriki.ti99.dskimg.io.FileDescriptorRecordIO;
import com.miriki.ti99.dskimg.io.Sector;
import com.miriki.ti99.dskimg.io.VolumeInformationBlockIO;

public final class Ti99FileSystemIO {

    private Ti99FileSystemIO() {}

    // ============================================================
    //  READ FILESYSTEM
    // ============================================================

    public static Ti99FileSystem read(Ti99Image image) {

        Ti99FileSystem fs = new Ti99FileSystem(image);

        // --- VIB ---
        Sector vibSector = image.getSector(VibConstants.VIB_SECTOR);
        VolumeInformationBlock vib = VolumeInformationBlockIO.readFrom(vibSector);
        fs.setVib(vib);

        // --- ABM ---
        int totalSectors = vib.getTotalSectors();
        fs.setAbm(AllocationBitmapIO.read(vibSector, totalSectors));

        // --- FDI ---
        Sector fdiSector = image.getSector(FdiConstants.FDI_SECTOR);
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
        Sector vibSector = image.getSector(VibConstants.VIB_SECTOR);
        VolumeInformationBlockIO.writeTo(vibSector, fs.getVib());

        // --- ABM ---
        AllocationBitmapIO.write(vibSector, fs.getAbm());

        // --- FDI ---
        Sector fdiSector = image.getSector(FdiConstants.FDI_SECTOR);
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
