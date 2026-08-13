package edu.pe.cibertec.saibm.catalog.migration;

import java.util.ArrayList;
import java.util.List;

public class BackfillReconciler {
    private final BackfillMapper mapper = new BackfillMapper();

    public ReconciliationReport reconcile(List<LegacyBookRow> source, List<CatalogBookSnapshot> target) {
        List<String> mismatches = new ArrayList<>();
        if (source.size() != target.size()) mismatches.add("count mismatch");
        var targetById = target.stream().collect(java.util.stream.Collectors.toMap(CatalogBookSnapshot::id, value -> value));
        for (LegacyBookRow row : source) {
            BackfillCandidate expected = mapper.map(row);
            CatalogBookSnapshot actual = targetById.get(expected.id());
            if (actual == null) {
                mismatches.add("missing id " + expected.id());
            } else if (!actual.title().equals(expected.title()) || !actual.description().equals(expected.description())
                    || !actual.author().equals(expected.author()) || !actual.imageUrl().equals(expected.imageUrl())
                    || actual.stock() != expected.stock()) {
                mismatches.add("mismatch id " + expected.id());
            }
        }
        return new ReconciliationReport(mismatches.isEmpty(), source.size(), target.size(), List.copyOf(mismatches));
    }
}
