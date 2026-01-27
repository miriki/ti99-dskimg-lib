package com.miriki.ti99.dskimg.io;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import com.miriki.ti99.dskimg.domain.FileDescriptorRecord;
import com.miriki.ti99.dskimg.domain.Ti99Image;
import com.miriki.ti99.dskimg.domain.constants.FdiConstants;

/**
 * High-level representation of the File Directory Information (FDI) sector.
 *
 * The FDI contains a simple list of File Descriptor Record (FDR) sector numbers.
 * Names are *not* stored in the FDI itself; they are resolved later by reading
 * the corresponding FDRs. Unknown names are represented as null.
 *
 * Layout (sector-sized):
 *   [0..1]   FDR sector #0 (big endian)
 *   [2..3]   FDR sector #1
 *   ...
 *   [n..n+1] 0x0000 terminator
 *
 * Only the sector numbers are stored. Names are maintained in-memory for sorting.
 */
public final class FdiDirectoryIO {

    /** Internal list of directory entries (name may be null). */
    private final List<DirectoryEntry> entries = new ArrayList<>();

    /**
     * One entry in the FDI: optional name + FDR sector number.
     */
    public record DirectoryEntry(String name, int fdrSector) {}

    private FdiDirectoryIO() {
        // Use static readFrom() to construct
    }

    // ============================================================
    //  LOAD FROM FDI SECTOR
    // ============================================================

    /**
     * Reads the FDI sector and returns an FdiDirectory object.
     */
    /*
    public static FdiDirectoryIO readFrom(Sector fdiSector) {
        Objects.requireNonNull(fdiSector, "fdiSector must not be null");

        FdiDirectoryIO dir = new FdiDirectoryIO();

        byte[] raw = fdiSector.getBytes();
        // int size = FdiConstants.FDI_SIZE;
        int off = 0;

        while (off + 1 < raw.length) {
            int fdrSector = ((raw[off] & 0xFF) << 8) | (raw[off + 1] & 0xFF);

            if (fdrSector == 0) {
                break; // terminator
            }

            dir.entries.add(new DirectoryEntry(null, fdrSector));
            off += 2;
        }

        return dir;
    }
    */

    public static FdiDirectoryIO readFrom(Sector fdiSector, Ti99Image image) {
        Objects.requireNonNull(fdiSector);
        Objects.requireNonNull(image);

        FdiDirectoryIO dir = new FdiDirectoryIO();

        byte[] raw = fdiSector.getBytes();
        int off = 0;

        while (off + 1 < raw.length) {
            int fdrSector = ((raw[off] & 0xFF) << 8) | (raw[off + 1] & 0xFF);
            if (fdrSector == 0) break;

            // FDR lesen
            FileDescriptorRecord fdr = FileDescriptorRecordIO.readFrom(image.getSector(fdrSector));

            // Name aus FDR holen
            String name = fdr.getFileName();

            dir.entries.add(new DirectoryEntry(name, fdrSector));
            off += 2;
        }

        return dir;
    }

    // ============================================================
    //  ADD ENTRY
    // ============================================================

    public void add(String name, int fdrSector) {
        Objects.requireNonNull(name, "name must not be null");
        entries.add(new DirectoryEntry(name, fdrSector));
    }

    // ============================================================
    //  REMOVE ENTRY
    // ============================================================

    public void remove(String name) {
        Objects.requireNonNull(name, "name must not be null");
        entries.removeIf(e -> name.equalsIgnoreCase(e.name()));
    }

    // ============================================================
    //  RENAME ENTRY
    // ============================================================

    public void rename(String oldName, String newName) {
        Objects.requireNonNull(oldName, "oldName must not be null");
        Objects.requireNonNull(newName, "newName must not be null");

        for (int i = 0; i < entries.size(); i++) {
            DirectoryEntry e = entries.get(i);
            if (oldName.equalsIgnoreCase(e.name())) {
                entries.set(i, new DirectoryEntry(newName, e.fdrSector()));
                return;
            }
        }
    }

    // ============================================================
    //  WRITE BACK TO FDI SECTOR (SORTED)
    // ============================================================

    /**
     * Writes the directory back into the FDI sector.
     *
     * Sorting rules:
     *   - entries with names come first (alphabetically)
     *   - entries with null names are ignored (not written)
     */
    public void writeTo(Sector fdiSector) {
        Objects.requireNonNull(fdiSector, "fdiSector must not be null");

        byte[] raw = new byte[FdiConstants.FDI_SIZE];

        // Sort: names first, alphabetically; null names last
        entries.sort(Comparator.comparing(
                DirectoryEntry::name,
                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
        ));

        int off = 0;

        for (DirectoryEntry e : entries) {
            if (e.name() == null) {
                continue; // skip unknown entries
            }

            if (off + 1 >= raw.length) {
                break; // no more space
            }

            raw[off]     = (byte) ((e.fdrSector() >> 8) & 0xFF);
            raw[off + 1] = (byte) (e.fdrSector() & 0xFF);
            off += 2;
        }

        // Terminator
        if (off + 1 < raw.length) {
            raw[off] = 0;
            raw[off + 1] = 0;
        }

        // Write back to sector
        for (int i = 0; i < raw.length; i++) {
            fdiSector.set(i, raw[i]);
        }
    }

    // ============================================================
    //  GET / UPDATE ENTRIES
    // ============================================================

    public void setNameForSector(int sector, String name) {
        Objects.requireNonNull(name, "name must not be null");

        for (int i = 0; i < entries.size(); i++) {
            DirectoryEntry e = entries.get(i);
            if (e.fdrSector() == sector) {
                entries.set(i, new DirectoryEntry(name, sector));
                return;
            }
        }
    }

    public List<DirectoryEntry> getEntries() {
        return List.copyOf(entries);
    }
}
