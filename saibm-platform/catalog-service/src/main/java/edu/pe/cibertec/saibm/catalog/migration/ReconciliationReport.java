package edu.pe.cibertec.saibm.catalog.migration;

import java.util.List;

public record ReconciliationReport(boolean reconciled, int sourceCount, int targetCount, List<String> mismatches) {
}
