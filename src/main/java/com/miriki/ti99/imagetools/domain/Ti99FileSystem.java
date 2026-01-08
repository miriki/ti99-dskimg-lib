package com.miriki.ti99.imagetools.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * High-level representation of a TI‑99 filesystem inside a disk image.
 *
 * This class aggregates the three core filesystem structures:
 *   - VIB (Volume Information Block)
 *   - ABM (Allocation Bitmap)
 *   - FDI (File Descriptor Index)
 *
 * plus the fully decoded list of FileDescriptorRecord objects.
 *
 * It contains no IO logic; reading/writing is handled by dedicated
 * reader/writer classes. This class is a pure in-memory model.
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
    //  CONSTRUCTOR
    // ============================================================

    /**
     * Creates a filesystem wrapper for the given disk image.
     *
     * The filesystem is initially empty; VIB/ABM/FDI must be set
     * by the reader before the filesystem is considered complete.
     */
    public Ti99FileSystem(Ti99Image image) {
        log.debug("[constructor] Ti99FileSystem({})", image);
        this.image = Objects.requireNonNull(image, "Ti99Image must not be null");
    }

    // ============================================================
    //  BASIC ACCESSORS
    // ============================================================

    public Ti99Image getImage() {
        log.debug("getImage()");
        return image;
    }

    public VolumeInformationBlock getVib() {
        log.debug("getVib()");
        return vib;
    }

    public AllocationBitmap getAbm() {
        log.debug("getAbm()");
        return abm;
    }

    public FileDescriptorIndex getFdi() {
        log.debug("getFdi()");
        return fdi;
    }

    public List<FileDescriptorRecord> getFiles() {
        log.debug("getFiles()");
        return List.copyOf(files);
    }

    // ============================================================
    //  MUTATORS
    // ============================================================

    public void setVib(VolumeInformationBlock vib) {
        log.debug("setVib({})", vib);
        this.vib = Objects.requireNonNull(vib, "VIB must not be null");
    }

    public void setAbm(AllocationBitmap abm) {
        log.debug("setAbm({})", abm);
        this.abm = Objects.requireNonNull(abm, "ABM must not be null");
    }

    public void setFdi(FileDescriptorIndex fdi) {
        log.debug("setFdi({})", fdi);
        this.fdi = Objects.requireNonNull(fdi, "FDI must not be null");
    }

    public void addFile(FileDescriptorRecord fdr) {
        log.debug("addFile({})", fdr);
        files.add(Objects.requireNonNull(fdr, "FileDescriptorRecord must not be null"));
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
        log.debug("validate()");

        if (vib == null) throw new IllegalStateException("VIB missing");
        if (abm == null) throw new IllegalStateException("ABM missing");
        if (fdi == null) throw new IllegalStateException("FDI missing");
    }

    /**
     * Returns true if VIB, ABM, and FDI have been set.
     */
    public boolean isComplete() {
        log.debug("isComplete()");
        return vib != null && abm != null && fdi != null;
    }
}
