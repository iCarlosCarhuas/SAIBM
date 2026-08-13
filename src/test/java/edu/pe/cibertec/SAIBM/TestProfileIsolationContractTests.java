package edu.pe.cibertec.SAIBM;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class TestProfileIsolationContractTests {

	@Test
	void exclusiveTestConfigurationHasOnlySafeTestSettings() throws IOException {
		InputStream resource = getClass().getResourceAsStream("/application-test.properties");

		assertNotNull(resource, "The exclusive test configuration must exist");
		String properties = new String(resource.readAllBytes(), StandardCharsets.UTF_8);

		assertTrue(properties.contains("recaptcha.secret=TEST_ONLY_UNUSABLE"));
		assertTrue(properties.contains("recaptcha.sitekey=TEST_ONLY_UNUSABLE"));
        assertTrue(properties.contains("spring.jpa.hibernate.ddl-auto=create-drop"));
        assertTrue(properties.contains("spring.sql.init.mode=never"));
        assertFalse(properties.contains("spring.sql.init.data-locations="));
        assertFalse(properties.contains("spring.jpa.defer-datasource-initialization="));
		assertFalse(properties.contains("spring.datasource.url="));
		assertFalse(properties.contains("spring.datasource.username="));
		assertFalse(properties.contains("spring.datasource.password="));
		assertFalse(properties.contains("spring.config.import="));
	}
}
