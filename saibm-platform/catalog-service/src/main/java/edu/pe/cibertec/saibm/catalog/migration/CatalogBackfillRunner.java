package edu.pe.cibertec.saibm.catalog.migration;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Explicit opt-in placeholder; normal catalog startup does not read legacy MySQL. */
@Component
@ConditionalOnProperty(name = "catalog.backfill.enabled", havingValue = "true")
public class CatalogBackfillRunner implements CommandLineRunner {
    private final CatalogBackfillService service;

    public CatalogBackfillRunner(CatalogBackfillService service) {
        this.service = service;
    }

    @Override
    public void run(String... args) {
        service.execute();
    }
}
