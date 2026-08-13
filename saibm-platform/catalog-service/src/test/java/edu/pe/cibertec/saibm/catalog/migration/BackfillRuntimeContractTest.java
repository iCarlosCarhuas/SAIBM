package edu.pe.cibertec.saibm.catalog.migration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class BackfillRuntimeContractTest {
    private static final Path ROOT = Path.of(System.getProperty("user.dir"));

    @Test
    void backfillIsOptInAndLegacyReaderIsNotCreatedByNormalStartup() throws IOException {
        Path root = repositoryRoot();
        String configuration = Files.readString(root.resolve("saibm-platform/catalog-service/src/main/resources/application.yml"));
        String source = Files.readString(root.resolve("saibm-platform/catalog-service/src/main/java/edu/pe/cibertec/saibm/catalog/migration/LegacyBookReader.java"));

        assertThat(configuration).contains("SAIBM_CATALOG_BACKFILL_ENABLED:false");
        assertThat(source).contains("select libro_id", "from libros").doesNotContain("update libros", "insert into libros", "delete from libros");
    }

    @Test
    void composeAndGatewayExposeOnlyExplicitCatalogRoutesAndKeepDbPrivate() throws IOException {
        Path root = repositoryRoot();
        String compose = Files.readString(root.resolve("compose.yaml"));
        String gateway = Files.readString(root.resolve("saibm-platform/api-gateway/src/main/resources/application.yml"));

        assertThat(compose).contains("catalog-service:", "catalog-db", "SAIBM_CATALOG_DB_PASSWORD", "SAIBM_CATALOG_INTERNAL_ADMIN_SECRET");
        assertThat(gateway).contains("lb://inventario-service", "/api/v1/books/**", "/api/v1/inventory/**")
                .doesNotContain("lb://catalog-service", "catalog-inventory")
                .doesNotContain("discovery.locator", "Path=/**");
        assertThat(serviceBlock(compose, "catalog-db")).doesNotContain("ports:");
        assertThat(serviceBlock(compose, "catalog-service")).doesNotContain("ports:");
    }

    private String serviceBlock(String compose, String service) {
        int start = compose.indexOf("  " + service + ":");
        int end = compose.indexOf("\n  ", start + service.length() + 3);
        return compose.substring(start, end < 0 ? compose.length() : end);
    }

    private Path repositoryRoot() {
        Path directory = ROOT;
        while (directory != null && !Files.exists(directory.resolve("compose.yaml"))) {
            directory = directory.getParent();
        }
        return directory;
    }
}
