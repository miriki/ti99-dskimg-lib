package com.miriki.ti99.dskimg.io;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.miriki.ti99.dskimg.domain.Ti99Image;
import com.miriki.ti99.dskimg.domain.constants.FdrConstants;

/**
 * Reads the HFDC Data Chain Pointer (DCP) block stored inside a File Descriptor Record (FDR).
 *
 * A DCP block begins at byte offset 28 inside the FDR and contains up to 76 entries.
 * Each entry is 3 bytes, representing a 24-bit cluster number in big-endian order.
 *
 * A value of 0 marks the end of the chain.
 */
public final class DataChainBlockIO {

    private DataChainBlockIO() {}

    // ============================================================
    //  PUBLIC API – READ CLUSTER CHAIN
    // ============================================================

    /**
     * Reads the HFDC cluster chain from the DCP block inside the FDR sector.
     *
     * @param image            the TI-99 disk image
     * @param fdrSectorNumber  sector number of the FDR
     * @return list of cluster numbers (empty if no chain exists)
     */
    public static List<Integer> readClusterChain(Ti99Image image, int fdrSectorNumber) {
        Objects.requireNonNull(image, "image must not be null");

        if (fdrSectorNumber < 0 || fdrSectorNumber >= image.getTotalSectors()) {
            throw new IndexOutOfBoundsException("Invalid FDR sector: " + fdrSectorNumber);
        }

        byte[] raw = image.getSector(fdrSectorNumber).getBytes();
        List<Integer> chain = new ArrayList<>();

        for (int i = 0; i < FdrConstants.DCP_ENTRY_COUNT; i++) {
            int cluster = readClusterEntry(raw, i);

            if (cluster == 0) {
                break;
            }

            chain.add(cluster);
        }

        return chain;
    }

    public static void writeClusterChain(Ti99Image image, int fdrSector, List<Integer> clusters) {
        // 1) DCP-Sektor bestimmen
        int dcpSector = fdrSector + 1; // oder wie dein Format es definiert

        // 2) Sektor holen
        Sector dcp = image.getSector(dcpSector);

        // 3) Clusterliste schreiben
        byte[] raw = dcp.getBytes();
        int pos = 0;

        for (int c : clusters) {
            raw[pos++] = (byte) ((c >> 8) & 0xFF);
            raw[pos++] = (byte) (c & 0xFF);
        }

        // Terminator
        raw[pos++] = 0;
        raw[pos++] = 0;

        // zurückschreiben
        for (int i = 0; i < raw.length; i++) {
            dcp.set(i, raw[i]);
        }
    }

    // ============================================================
    //  INTERNAL HELPERS
    // ============================================================

    /**
     * Reads a single 3-byte DCP entry and returns the 24-bit cluster number.
     * HFDC uses big-endian order: b0 = high byte.
     */
    private static int readClusterEntry(byte[] raw, int index) {
        int base = FdrConstants.FDR_DCP_OFFSET + index * FdrConstants.DCP_ENTRY_SIZE;

        int b0 = raw[base]     & 0xFF;
        int b1 = raw[base + 1] & 0xFF;
        int b2 = raw[base + 2] & 0xFF;

        return (b0 << 16) | (b1 << 8) | b2;
    }
}
