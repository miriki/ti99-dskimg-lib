package com.miriki.ti99.dskimg.domain.fscheck;

import com.miriki.ti99.dskimg.domain.Ti99FileSystem;

public final class DefaultHealthScoreCalculator implements HealthScoreCalculator {

    @Override
    public int calculate(Ti99FileSystem fs) {

        int score = 100;

        // ============================================================
        //  VITALWERT 1: Fragmentierung
        // ============================================================
        int fragmentation = calculateFragmentation(fs); // TODO
        score -= fragmentation;

        // ============================================================
        //  VITALWERT 2: Orphan-Cluster
        // ============================================================
        int orphanCount = countOrphanClusters(fs); // TODO
        score -= orphanCount * 2;

        // ============================================================
        //  VITALWERT 3: Cross-Links
        // ============================================================
        int crossLinks = countCrossLinks(fs); // TODO
        score -= crossLinks * 5;

        // ============================================================
        //  VITALWERT 4: ABM-Konsistenz
        // ============================================================
        int abmIssues = countAbmInconsistencies(fs); // TODO
        score -= abmIssues * 3;

        // ============================================================
        //  VITALWERT 5: FDI-Integrität
        // ============================================================
        int fdiIssues = countInvalidFdiEntries(fs); // TODO
        score -= fdiIssues * 2;

        // ============================================================
        //  VITALWERT 6: FDR-Integrität
        // ============================================================
        int fdrIssues = countInvalidFdrs(fs); // TODO
        score -= fdrIssues * 2;

        // ============================================================
        //  VITALWERT 7: Freier Speicher
        // ============================================================
        int freeSpacePenalty = calculateFreeSpacePenalty(fs); // TODO
        score -= freeSpacePenalty;

        // ============================================================
        //  VITALWERT 8: Größte freie Lücke
        // ============================================================
        int fragmentationPenalty = calculateLargestFreeBlockPenalty(fs); // TODO
        score -= fragmentationPenalty;

        // Score clampen
        if (score < 0) score = 0;
        if (score > 100) score = 100;

        return score;
    }

    // ============================================================
    //  TODO-BEREICH – HIER KOMMT SPÄTER DIE ECHTE LOGIK HIN
    // ============================================================

    private int calculateFragmentation(Ti99FileSystem fs) {
        // TODO: Fragmentierungsgrad berechnen (0–20)
        return 0;
    }

    private int countOrphanClusters(Ti99FileSystem fs) {
        // TODO: Orphan-Cluster zählen
        return 0;
    }

    private int countCrossLinks(Ti99FileSystem fs) {
        // TODO: Cross-Links zählen
        return 0;
    }

    private int countAbmInconsistencies(Ti99FileSystem fs) {
        // TODO: ABM/FDR-Abweichungen zählen
        return 0;
    }

    private int countInvalidFdiEntries(Ti99FileSystem fs) {
        // TODO: Ungültige FDI-Einträge zählen
        return 0;
    }

    private int countInvalidFdrs(Ti99FileSystem fs) {
        // TODO: Ungültige FDRs zählen
        return 0;
    }

    private int calculateFreeSpacePenalty(Ti99FileSystem fs) {
        // TODO: Wenig freier Speicher = schlechter Score
        return 0;
    }

    private int calculateLargestFreeBlockPenalty(Ti99FileSystem fs) {
        // TODO: Kleine freie Lücken = schlechter Score
        return 0;
    }
}
