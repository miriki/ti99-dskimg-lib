package com.miriki.ti99.dskimg.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.miriki.ti99.dskimg.domain.fscheck.FilesystemCheckResult;
import com.miriki.ti99.dskimg.domain.fscheck.FilesystemHealth;
import com.miriki.ti99.dskimg.io.FileDescriptorRecordIO;

/**
 * High-level in-memory representation of a TI‑99 filesystem inside a disk image.
 *
 * Aggregates the core filesystem structures:
 *   - VIB (Volume Information Block)
 *   - ABM (Allocation Bitmap)
 *   - FDI (File Descriptor Index)
 *
 * plus the fully decoded list of FileDescriptorRecord objects.
 *
 * This class contains no IO logic; reading/writing is handled by dedicated
 * reader/writer classes. It is a pure model and the single source of truth
 * for higher-level operations (FileWriter, DirectoryReader, checks, etc.).
 */
public final class Ti99FileSystem {

    private static final Logger log = LoggerFactory.getLogger(Ti99FileSystem.class);

    // ============================================================
    //  CORE IMAGE REFERENCE
    // ============================================================

    /** Underlying disk image. Immutable reference. */
    private final Ti99Image image;

    // ============================================================
    //  FILESYSTEM STRUCTURES
    // ============================================================

    private VolumeInformationBlock vib;
    private AllocationBitmap abm;
    private FileDescriptorIndex fdi;

    /** Fully decoded FDRs (in directory order). */
    private final List<FileDescriptorRecord> files = new ArrayList<>();

    // ============================================================
    //  CONSTRUCTORS
    // ============================================================

    /**
     * Creates an empty filesystem wrapper for the given disk image.
     * VIB/ABM/FDI must be set before the filesystem is considered complete.
     */
    public Ti99FileSystem(Ti99Image image) {
        this.image = Objects.requireNonNull(image, "Ti99Image must not be null");
    }

    /**
     * Convenience constructor when VIB is already known.
     * ABM is derived from the VIB.
     */
    public Ti99FileSystem(Ti99Image image, VolumeInformationBlock vib) {
        this.image = Objects.requireNonNull(image, "Ti99Image must not be null");
        this.vib   = Objects.requireNonNull(vib, "VIB must not be null");
        this.abm   = Objects.requireNonNull(vib.getAbm(), "ABM from VIB must not be null");
    }

    // ============================================================
    //  BASIC ACCESSORS
    // ============================================================

    public Ti99Image getImage() {
        return image;
    }

    public VolumeInformationBlock getVib() {
        return vib;
    }

    public AllocationBitmap getAbm() {
        return abm;
    }

    public FileDescriptorIndex getFdi() {
        return fdi;
    }

    /**
     * Returns an immutable snapshot of the file list.
     */
    public List<FileDescriptorRecord> getFiles() {
        return List.copyOf(files);
    }

    // ============================================================
    //  MUTATORS
    // ============================================================

    public void setVib(VolumeInformationBlock vib) {
        this.vib = Objects.requireNonNull(vib, "VIB must not be null");
    }

    public void setAbm(AllocationBitmap abm) {
        this.abm = Objects.requireNonNull(abm, "ABM must not be null");
    }

    public void setFdi(FileDescriptorIndex fdi) {
        this.fdi = Objects.requireNonNull(fdi, "FDI must not be null");
    }

    public void addFile(FileDescriptorRecord fdr) {
        Objects.requireNonNull(fdr, "FileDescriptorRecord must not be null");
        files.add(fdr);
    }

    // ============================================================
    //  VALIDATION
    // ============================================================

    /**
     * Ensures that all required filesystem structures are present.
     *
     * @throws IllegalStateException if any component is missing
     */
    public void validate() {
        if (vib == null) throw new IllegalStateException("VIB missing");
        if (abm == null) throw new IllegalStateException("ABM missing");
        if (fdi == null) throw new IllegalStateException("FDI missing");
    }

    /**
     * Returns true if VIB, ABM, and FDI have been set.
     */
    public boolean isComplete() {
        return vib != null && abm != null && fdi != null;
    }

    // ============================================================
    //  CONVENIENCE – NAME NORMALIZATION
    // ============================================================

    private static String normalizeName(String name) {
        return Objects.requireNonNull(name, "name must not be null").trim().toUpperCase();
    }

    // ============================================================
    //  CONVENIENCE – FILE LOOKUP
    // ============================================================

    /**
     * Returns the file with the given name (case-insensitive), or empty if not found.
     */
    public Optional<FileDescriptorRecord> findFile(String name) {
        String norm = normalizeName(name);
        return files.stream()
                .filter(f -> norm.equals(normalizeName(f.getFileName())))
                .findFirst();
    }

    /**
     * Returns the file with the given name, or null if not found.
     * Prefer {@link #findFile(String)} in new code.
     */
    public FileDescriptorRecord getFileByName(String name) {
        return findFile(name).orElse(null);
    }

    /** Returns true if a file with the given name exists. */
    public boolean hasFile(String name) {
        return findFile(name).isPresent();
    }

    // ============================================================
    //  CONVENIENCE – FILE REMOVAL / RENAME
    // ============================================================

    /** Removes the given FDR from the filesystem. */
    public boolean removeFile(FileDescriptorRecord fdr) {
        Objects.requireNonNull(fdr, "FileDescriptorRecord must not be null");
        return files.remove(fdr);
    }

    /** Removes a file by name. Returns true if removed. */
    public boolean removeFileByName(String name) {
        return findFile(name)
                .map(files::remove)
                .orElse(false);
    }

    /**
     * Renames a file in the in-memory model only.
     * Does not touch FDI or on-disk structures.
     */
    public boolean renameFile(String oldName, String newName) {
        FileDescriptorRecord fdr = getFileByName(oldName);
        if (fdr == null) {
            return false;
        }
        fdr.setFileName(newName);
        return true;
    }

    // ============================================================
    //  CONVENIENCE – FILE LIST INFO
    // ============================================================

    /** Returns the number of files in the filesystem. */
    public int getFileCount() {
        return files.size();
    }

    /** Returns a sorted snapshot of the file list (case-insensitive by name). */
    public List<FileDescriptorRecord> getFilesSortedByName() {
        return files.stream()
                .sorted((a, b) -> a.getFileName().compareToIgnoreCase(b.getFileName()))
                .toList();
    }

    // ============================================================
    //  CONVENIENCE – FDI SUPPORT
    // ============================================================

    /**
     * Finds the first free FDI slot (index where FDR sector = 0).
     * Returns -1 if no free slot exists or FDI is not set.
     */
    public int findFreeFdrSlot() {
        if (fdi == null) {
            return -1;
        }
        int[] sectors = fdi.getFdrSectors();
        for (int i = 0; i < sectors.length; i++) {
            if (sectors[i] == 0) {
                return i;
            }
        }
        return -1;
    }

    // ============================================================
    //  CHECK / REPAIR HOOKS (INFRASTRUCTURE PREPARED)
    // ============================================================

    /**
     * Runs filesystem checks and returns a result object.
     * Currently a placeholder – real logic can be plugged in later.
     */
    public FilesystemCheckResult checkFilesystem() {
        // Placeholder: later you can delegate to a FilesystemChecker implementation
        return new FilesystemCheckResult(FilesystemHealth.GOOD, List.of());
    }

    /**
     * Applies repairs based on a previous check result.
     * Currently a placeholder – real logic can be plugged in later.
     */
    public void repairFilesystem(FilesystemCheckResult result) {
        Objects.requireNonNull(result, "FilesystemCheckResult must not be null");
        log.debug("repairFilesystem(): {} issues reported, no automatic repair implemented yet",
                result.issues().size());
    }

    public int getSectorForFile(String name) {
        Objects.requireNonNull(name, "name must not be null");
        String norm = name.trim().toUpperCase();

        int[] sectors = fdi.getFdrSectors();

        for (int sector : sectors) {
            if (sector == 0) continue;

            FileDescriptorRecord fdr =
                    FileDescriptorRecordIO.readFrom(image.getSector(sector));

            if (fdr.getFileName().trim().toUpperCase().equals(norm)) {
                return sector;
            }
        }

        return -1;
    }

}
