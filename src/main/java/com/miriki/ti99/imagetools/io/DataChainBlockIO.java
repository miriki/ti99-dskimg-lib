package com.miriki.ti99.imagetools.io;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.miriki.ti99.imagetools.domain.Ti99Image;

/**
 * IO helper for reading HFDC Data Chain Pointer (DCP) blocks.
 *
 * A DCP block is stored inside the File Descriptor Record (FDR) starting at
 * byte offset 28. It consists of 76 entries, each 3 bytes long:
 *
 *   Byte layout of one DCP entry (3 bytes):
 *     b0: high byte of cluster number
 *     b1: mid byte
 *     b2: low byte
 *
 *   Combined into a 24-bit cluster number:
 *     cluster = (b0 << 16) | (b1 << 8) | b2
 *
 * A value of 0 marks the end of the chain.
 */
public final class DataChainBlockIO {

    private static final Logger log = LoggerFactory.getLogger(DataChainBlockIO.class);

    /** Offset of the DCP block inside the FDR */
    private static final int OFFSET = 28;

    /** Size of one DCP entry in bytes */
    private static final int ENTRY_SIZE = 3;

    /** Maximum number of DCP entries (76 × 3 = 228 bytes) */
    private static final int ENTRY_COUNT = 76;

    private DataChainBlockIO() {
        // Utility class – no instances
    }

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

        log.debug("readClusterChain(image={}, fdrSector={})", image, fdrSectorNumber);

        byte[] raw = image.getSector(fdrSectorNumber).getBytes();
        List<Integer> chain = new ArrayList<>();

        for (int i = 0; i < ENTRY_COUNT; i++) {
            int cluster = readClusterEntry(raw, i);

            // 0 marks the end of the chain
            if (cluster == 0) {
                break;
            }

            chain.add(cluster);
        }

        return chain;
    }

    // ============================================================
    //  INTERNAL HELPERS
    // ============================================================

    /**
     * Reads a single 3-byte DCP entry and returns the 24-bit cluster number.
     */
    private static int readClusterEntry(byte[] raw, int index) {
        int base = OFFSET + index * ENTRY_SIZE;

        int b0 = raw[base]     & 0xFF;
        int b1 = raw[base + 1] & 0xFF;
        int b2 = raw[base + 2] & 0xFF;

        return (b0 << 16) | (b1 << 8) | b2;
    }
}
