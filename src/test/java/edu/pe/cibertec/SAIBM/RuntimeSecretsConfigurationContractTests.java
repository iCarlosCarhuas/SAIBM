package edu.pe.cibertec.SAIBM;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.Test;

class RuntimeSecretsConfigurationContractTests {

	private static final Path APPLICATION_PROPERTIES = Path.of("src/main/resources/application.properties");
	private static final Path LOCAL_TEMPLATE = Path.of("env.properties.example");

	@Test
	void runtimeConfigurationRequiresExternalValuesWithoutSensitiveDefaults() throws IOException {
		Properties properties = loadProperties(APPLICATION_PROPERTIES);

		assertEquals("${SAIBM_DB_URL}", properties.getProperty("spring.datasource.url"));
		assertEquals("${SAIBM_DB_USER}", properties.getProperty("spring.datasource.username"));
		assertEquals("${SAIBM_DB_PASSWORD}", properties.getProperty("spring.datasource.password"));
		assertEquals("${SAIBM_RECAPTCHA_SITE_KEY}", properties.getProperty("recaptcha.sitekey"));
		assertEquals("${SAIBM_RECAPTCHA_SECRET}", properties.getProperty("recaptcha.secret"));
		assertEquals("optional:file:env.properties", properties.getProperty("spring.config.import"));
	}

	@Test
	void localSecretFileIsIgnoredWhileItsSafeTemplateRemainsTrackable() throws IOException {
		String gitignore = Files.readString(Path.of(".gitignore"));
		String template = Files.readString(LOCAL_TEMPLATE);

		assertTrue(gitignore.lines().anyMatch("env.properties"::equals));
		assertTrue(gitignore.lines().anyMatch("!env.properties.example"::equals));
		assertTrue(template.contains("REPLACE_WITH_DATABASE_PASSWORD"));
		assertTrue(template.contains("REPLACE_WITH_SERVER_SECRET"));
		assertFalse(template.toLowerCase().contains("password=admin"), "Template must not contain a usable password default");
	}

	@Test
	void trackedRuntimeTextDoesNotContainCredentialLikeLiterals() throws IOException {
		List<Path> files = List.of(APPLICATION_PROPERTIES, LOCAL_TEMPLATE);

		for (Path file : files) {
			String content = Files.readString(file);
			assertFalse(content.matches("(?s).*recaptcha\\.(sitekey|secret)=6L[A-Za-z0-9_-]{20,}.*"),
					() -> file + " must not contain a literal reCAPTCHA credential");
			assertFalse(content.matches("(?s).*SAIBM_DB_PASSWORD=(?!REPLACE_WITH_)[^${\\r\\n][^\\r\\n]*.*"),
					() -> file + " must not contain a usable database password");
		}
	}

	private Properties loadProperties(Path path) throws IOException {
		Properties properties = new Properties();
		try (var input = Files.newInputStream(path)) {
			properties.load(input);
		}
		return properties;
	}
}
