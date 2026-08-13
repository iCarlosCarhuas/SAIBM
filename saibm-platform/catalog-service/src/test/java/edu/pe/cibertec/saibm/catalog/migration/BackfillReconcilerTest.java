package edu.pe.cibertec.saibm.catalog.migration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class BackfillReconcilerTest {
    private final BackfillReconciler reconciler = new BackfillReconciler();

    @Test
    void matchingIdsFieldsAndStockReconcileCompletely() {
        LegacyBookRow source = new LegacyBookRow(7, "Java", null, 4, "Ada", null);
        CatalogBookSnapshot target = new CatalogBookSnapshot(7, "Java", "", "Ada", "", 4);

        ReconciliationReport report = reconciler.reconcile(List.of(source), List.of(target));

        assertThat(report.reconciled()).isTrue();
        assertThat(report.sourceCount()).isEqualTo(1);
        assertThat(report.targetCount()).isEqualTo(1);
        assertThat(report.mismatches()).isEmpty();
    }

    @Test
    void changedStockOrMissingIdBlocksCutover() {
        LegacyBookRow source = new LegacyBookRow(7, "Java", "", 4, "Ada", "");
        CatalogBookSnapshot target = new CatalogBookSnapshot(8, "Java", "", "Ada", "", 1);

        ReconciliationReport report = reconciler.reconcile(List.of(source), List.of(target));

        assertThat(report.reconciled()).isFalse();
        assertThat(report.mismatches()).isNotEmpty().anySatisfy(mismatch ->
                assertThat(mismatch).contains("7"));
    }
}
