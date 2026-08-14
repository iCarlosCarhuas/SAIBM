package edu.pe.cibertec.SAIBM;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

class LocalRuntimeContractTests {

	private static final Path ROOT = Path.of(System.getProperty("user.dir"));

	@Test
	void databasesStayPrivateAndInfrastructureIsHealthChecked() throws IOException {
		String compose = Files.readString(ROOT.resolve("compose.yaml"));
		for (String service : new String[] { "iam-db", "libro-db", "inventario-db", "membresia-db", "usuario-db", "circulation-db", "reporting-db" }) {
			String block = serviceBlock(compose, service);
			assertThat(block).contains("POSTGRES_DB: ${SAIBM_", "POSTGRES_USER: ${SAIBM_",
					"POSTGRES_PASSWORD: ${SAIBM_", ":?required}").doesNotContain("ports:");
		}
		assertThat(compose).contains("pg_isready", "rabbitmq-diagnostics", "service_healthy",
				"services: {internal: true}", "data: {internal: true}");
		assertThat(serviceBlock(compose, "inventario-service")).contains("healthcheck:", "rabbitmq", "inventario-db");
		assertThat(serviceBlock(compose, "membresia-service")).contains("healthcheck:", "rabbitmq", "membresia-db");
		assertThat(serviceBlock(compose, "usuario-service")).contains("healthcheck:", "rabbitmq", "usuario-db");
	}

	@Test
	void applicationsUseExternalConfigurationAndLocalSecretFileIsIgnored() throws IOException {
		String compose = Files.readString(ROOT.resolve("compose.yaml"));
		String ignore = Files.readString(ROOT.resolve(".gitignore"));
		assertThat(serviceBlock(compose, "api-gateway"))
				.contains("SAIBM_LEGACY_BASE_URL: ${SAIBM_LEGACY_BASE_URL:?required}",
						"127.0.0.1:8080:8080", "edge, services", "healthcheck:");
		assertThat(serviceBlock(compose, "discovery-server"))
				.contains("127.0.0.1:8761:8761", "healthcheck:");
		assertThat(ignore.lines().map(String::trim)).contains("env.properties");
	}

	@Test
	void everyPlatformDockerBuildIncludesAllRegisteredReactorDescriptors() throws IOException {
		String compose = Files.readString(ROOT.resolve("compose.yaml"));
		for (String service : new String[] { "discovery-server", "api-gateway", "libro-service",
				"inventario-service", "membresia-service", "usuario-service" }) {
			String dockerfile = Files.readString(ROOT.resolve("saibm-platform").resolve(service).resolve("Dockerfile"));
			assertThat(dockerfile).contains("membresia-service/pom.xml membresia-service/pom.xml",
					"usuario-service/pom.xml usuario-service/pom.xml");
		}
		assertThat(compose).doesNotContain("catalog-service", "catalog-db", "catalog-data");
	}

	private String serviceBlock(String compose, String service) {
		Pattern nextService = Pattern.compile("(?m)^  [a-z][a-z0-9-]+:\\s*$");
		Matcher serviceLine = Pattern.compile("(?m)^  " + Pattern.quote(service) + ":\\s*$").matcher(compose);
		if (!serviceLine.find()) {
			throw new IllegalArgumentException("Missing Compose service: " + service);
		}
		int start = serviceLine.start();
		var match = nextService.matcher(compose);
		int end = match.find(start + service.length() + 3) ? match.start() : compose.indexOf("\nnetworks:", start);
		return compose.substring(start, end);
	}
}
