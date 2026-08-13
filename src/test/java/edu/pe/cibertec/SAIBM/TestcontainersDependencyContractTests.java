package edu.pe.cibertec.SAIBM;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

class TestcontainersDependencyContractTests {

    private static final Path POM = Path.of("pom.xml");

    @Test
    void pinsTheAlignedGraphAndPreservesTheRuntimeBaseline() throws IOException {
        String pom = Files.readString(POM).replace("\r\n", "\n");

        assertTrue(pom.contains("<artifactId>testcontainers-bom</artifactId>\n\t\t\t\t<version>2.0.5</version>"));
        assertTrue(pom.contains("<version>3.5.3</version>"));
        assertTrue(pom.contains("<java.version>21</java.version>"));
        assertTrue(hasDependency(pom, "org.springframework.boot", "spring-boot-testcontainers"));
        assertTrue(hasDependency(pom, "org.testcontainers", "testcontainers-junit-jupiter"));
        assertTrue(hasDependency(pom, "org.testcontainers", "testcontainers-mysql"));
        assertFalse(hasDependency(pom, "org.testcontainers", "junit-jupiter"));
        assertFalse(hasDependency(pom, "org.testcontainers", "mysql"));
    }

    @Test
    void rejectsCompatibilityOverridesAndCleanupDisablement() throws IOException {
        String pom = Files.readString(POM).toLowerCase();
        for (String marker : new String[]{"docker-java", "docker_api_version", "docker.api.version",
                "testcontainers_ryuk_disabled", "testcontainers.ryuk.disabled"}) {
            assertFalse(pom.contains(marker), marker);
        }
    }

    private boolean hasDependency(String pom, String group, String artifact) {
        String expression = "(?s)<dependency>\\s*<groupId>" + Pattern.quote(group)
                + "</groupId>\\s*<artifactId>" + Pattern.quote(artifact) + "</artifactId>";
        return Pattern.compile(expression).matcher(pom).find();
    }
}
