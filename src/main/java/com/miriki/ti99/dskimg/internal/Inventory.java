package com.miriki.ti99.dskimg.internal;

/**
 * Architectural inventory of planned subsystems.
 *
 * This class does not contain logic. It documents the internal
 * components that will eventually form the complete implementation
 * of the TI-99/4A disk image library.
 *
 * Think of this as the "basement inventory" of the project.
 */
public final class Inventory {

    /**
     * 1. Domain parsing
     *    - Ti99ImageFactory
     *    - SectorReader / SectorWriter
     *    - DirectoryParser (FDR/VIB/FDI)
     */

    /**
     * 2. Filesystem operations
     *    - FileImporter
     *    - FileExporter
     *    - FileDeleter
     *    - FileRenamer
     */

    /**
     * 3. Allocation management
     *    - AllocationBitmapManager
     *    - ClusterAllocator
     *    - FreeSpaceFinder
     */

    /**
     * 4. DTO mapping
     *    - FdrToDtoMapper
     *    - VibToDtoMapper
     *    - AbmToDtoMapper
     */

    /**
     * 5. Filesystem check & repair
     *    - FilesystemChecker
     *    - FilesystemRepairEngine
     *    - ConsistencyRules
     */

    /**
     * 6. Low-level IO (future)
     *    - PhysicalAnomalies
     *    - ControllerBehavior
     *    - SectorEncoding
     *    - TrackLayout
     *    - CopyProtectionPatterns
     */

    /**
     * 7. Utilities
     *    - SectorMath
     *    - FilenameValidator
     *    - RecordFormatConverter
     */

    // No fields, no logic — this is a conceptual map of the project.
}
