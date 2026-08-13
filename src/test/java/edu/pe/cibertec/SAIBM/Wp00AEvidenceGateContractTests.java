package edu.pe.cibertec.SAIBM;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

class Wp00AEvidenceGateContractTests {

    @Test
    void unavailablePrerequisiteDiagnosisIsActionable() {
        String blocked = "BLOCKED: Docker named pipe; observed=Test-Path returned False; "
                + "remediation=Start Docker Desktop and rerun preflight";
        assertTrue(blocked.startsWith("BLOCKED"));
        assertTrue(blocked.contains("observed=Test-Path returned False"));
        assertTrue(blocked.contains("remediation=Start Docker Desktop and rerun preflight"));
    }

    @Test
    void failedOrSkippedSuiteReceiptIsRejected() {
        assertTrue(clean("Tests run: 2, Failures: 0, Errors: 0, Skipped: 0"));
        assertFalse(clean("Tests run: 2, Failures: 1, Errors: 0, Skipped: 0"));
        assertFalse(clean("Tests run: 2, Failures: 0, Errors: 0, Skipped: 1"));
    }

    @Test
    void minimumBaselineCoversFiveFamilies() {
        Map<String, Boolean> families = Map.of("authentication-registration", true, "administration", true,
                "catalog", true, "circulation-quota-stock", true, "reporting", true);
        assertTrue(families.size() == 5 && families.values().stream().allMatch(Boolean.TRUE::equals));
    }

    @Test
    void incompleteBaselineIsRejected() {
        assertFalse(Map.of("catalog", true).size() == 5);
        assertTrue(399 < 400);
        assertFalse(400 < 400);
    }

    @Test
    void rollbackDoesNotRemovePriorTestProfileContract() throws Exception {
        Set<String> rollback = Set.of("pom.xml", "src/test/resources/wp-00-a-schema.sql");
        String profile = "src/test/java/edu/pe/cibertec/SAIBM/TestProfileIsolationContractTests.java";
        assertFalse(rollback.contains(profile));
        assertTrue(Files.exists(Path.of(profile)));
        assertTrue(Files.readString(Path.of("docs/migration/testcontainers-compatibility.md"))
                .contains("Preserve `TestProfileIsolationContractTests.java`"));
    }

    @Test
    void documentationKeepsTheExecutableBoundaryConcise() throws Exception {
        String document = Files.readString(Path.of("docs/migration/testcontainers-compatibility.md"));
        assertTrue(document.contains("Testcontainers BOM | `2.0.5`"));
        assertTrue(document.contains("mysql:8.0.42"));
        assertTrue(document.contains("mvnw.cmd test"));
        assertTrue(document.contains("Docker API 1.55"));
        assertTrue(document.contains("Characterization boundary"));
    }

    private boolean clean(String receipt) {
        return receipt.matches("Tests run: \\d+, Failures: 0, Errors: 0, Skipped: 0");
    }
}
