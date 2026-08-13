package edu.pe.cibertec.saibm.catalog.migration;

public class BackfillMapper {
    public BackfillCandidate map(LegacyBookRow row) {
        if (row.id() == null || row.id() <= 0) throw new BackfillValidationException("libro_id must be positive");
        if (row.stock() == null || row.stock() < 0) throw new BackfillValidationException("stock must be non-negative");
        return new BackfillCandidate(row.id(), text(row.title()), text(row.description()), text(row.author()),
                text(row.imageUrl()), row.stock());
    }

    private String text(String value) { return value == null ? "" : value; }
}
