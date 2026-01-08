package com.miriki.ti99.imagetools.fs;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.miriki.ti99.imagetools.io.Sector;

/**
 * High-level representation of the File Directory Information (FDI) sector.
 *
 * The FDI contains a simple list of File Descriptor Record (FDR) sector numbers.
 * Names are *not* stored in the FDI itself; they are resolved later by reading
 * the corresponding FDRs. Unknown names are represented as null.
 *
 * Layout (256 bytes):
 *   [0..1]   FDR sector #0 (big endian)
 *   [2..3]   FDR sector #1
 *   ...
 *   [n..n+1] 0x0000 terminator
 *
 * Only the sector numbers are stored. Names are maintained in-memory for sorting.
 */
public final class FdiDirectory {

    /** Internal list of directory entries (name may be null). */
    private final List<DirectoryEntry> entries = new ArrayList<>();

    /**
     * One entry in the FDI: optional name + FDR sector number.
     */
    public record DirectoryEntry(String name, int fdrSector) {}

    private FdiDirectory() {
        // Use static readFrom() to construct
    }

    // ============================================================
    //  LOAD FROM FDI SECTOR
    // ============================================================

    /**
     * Reads the FDI sector and returns an FdiDirectory object.
     *
     * @param fdiSector the sector containing the FDI
     * @return parsed FdiDirectory
     */
    public static FdiDirectory readFrom(Sector fdiSector) {
        Objects.requireNonNull(fdiSector, "fdiSector must not be null");

        FdiDirectory dir = new FdiDirectory();

        byte[] raw = fdiSector.getBytes();
        int off = 0;

        while (off < 256) {
            int fdrSector = ((raw[off] & 0xFF) << 8) | (raw[off + 1] & 0xFF);

            if (fdrSector == 0) {
                break; // terminator
            }

            // Name unknown at this stage → null
            dir.entries.add(new DirectoryEntry(null, fdrSector));

            off += 2;
        }

        return dir;
    }

    // ============================================================
    //  ADD ENTRY
    // ============================================================

    /**
     * Adds a new entry (name + FDR sector).
     */
    public void add(String name, int fdrSector) {
        Objects.requireNonNull(name, "name must not be null");
        entries.add(new DirectoryEntry(name, fdrSector));
    }

    // ============================================================
    //  REMOVE ENTRY
    // ============================================================

    /**
     * Removes an entry by name (case-insensitive).
     */
    public void remove(String name) {
        Objects.requireNonNull(name, "name must not be null");
        entries.removeIf(e -> name.equalsIgnoreCase(e.name()));
    }

    // ============================================================
    //  RENAME ENTRY
    // ============================================================

    /**
     * Renames an entry (case-insensitive).
     */
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

        byte[] raw = new byte[256];

        // Sort only entries with names
        entries.sort((a, b) -> {
            if (a.name() == null && b.name() == null) return 0;
            if (a.name() == null) return 1;   // null → end
            if (b.name() == null) return -1;
            return a.name().compareToIgnoreCase(b.name());
        });

        int off = 0;

        for (DirectoryEntry e : entries) {
            if (e.name() == null) {
                continue; // skip unknown entries
            }

            raw[off]     = (byte) ((e.fdrSector() >> 8) & 0xFF);
            raw[off + 1] = (byte) (e.fdrSector() & 0xFF);
            off += 2;

            if (off >= 256) break;
        }

        // Terminator
        if (off < 256) {
            raw[off] = 0;
            raw[off + 1] = 0;
        }

        // Write back to sector
        for (int i = 0; i < 256; i++) {
            fdiSector.set(i, raw[i]);
        }
    }

    // ============================================================
    //  GET / UPDATE ENTRIES
    // ============================================================

    /**
     * Sets the name for a given FDR sector.
     */
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

    /**
     * Returns an immutable copy of all directory entries.
     */
    public List<DirectoryEntry> getEntries() {
        return List.copyOf(entries);
    }
}
