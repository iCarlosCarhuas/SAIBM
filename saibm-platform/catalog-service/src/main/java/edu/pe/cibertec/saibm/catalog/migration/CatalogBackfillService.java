package edu.pe.cibertec.saibm.catalog.migration;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(name = "catalog.backfill.enabled", havingValue = "true")
public class CatalogBackfillService {
    private final LegacyBookReader legacy;
    private final JdbcTemplate catalog;
    private final BackfillMapper mapper = new BackfillMapper();
    private final BackfillReconciler reconciler = new BackfillReconciler();

    public CatalogBackfillService(LegacyBookReader legacy, @Qualifier("dataSource") DataSource catalogDataSource) {
        this.legacy = legacy;
        this.catalog = new JdbcTemplate(catalogDataSource);
    }

    @Transactional
    public ReconciliationReport execute() {
        List<LegacyBookRow> source = legacy.read();
        for (LegacyBookRow row : source) {
            BackfillCandidate candidate = mapper.map(row);
            catalog.update("insert into books (id, title, description, author, image_url, active) "
                    + "values (?, ?, ?, ?, ?, true) on conflict (id) do update set title = excluded.title, "
                    + "description = excluded.description, author = excluded.author, image_url = excluded.image_url, active = true",
                    candidate.id(), candidate.title(), candidate.description(), candidate.author(), candidate.imageUrl());
            catalog.update("insert into inventory (book_id, total_stock, available_stock) values (?, ?, ?) "
                    + "on conflict (book_id) do update set total_stock = excluded.total_stock, "
                    + "available_stock = excluded.available_stock", candidate.id(), candidate.stock(), candidate.stock());
        }
        catalog.queryForObject("select setval(pg_get_serial_sequence('books', 'id'), "
                + "coalesce((select max(id) from books), 1), true)", Long.class);

        List<CatalogBookSnapshot> target = catalog.query("select b.id, b.title, b.description, b.author, "
                + "b.image_url, i.total_stock from books b join inventory i on i.book_id = b.id",
                (rs, rowNum) -> new CatalogBookSnapshot(rs.getInt("id"), rs.getString("title"),
                        rs.getString("description"), rs.getString("author"), rs.getString("image_url"),
                        rs.getInt("total_stock")));
        ReconciliationReport report = reconciler.reconcile(source, target);
        if (!report.reconciled()) {
            throw new BackfillValidationException("Catalog backfill reconciliation failed");
        }
        return report;
    }
}
