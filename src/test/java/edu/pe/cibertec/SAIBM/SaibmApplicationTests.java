package edu.pe.cibertec.SAIBM;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.JdbcConnectionDetails;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@Sql(scripts = "/wp-00-a-schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@SpringBootTest(properties = "spring.config.location=classpath:/application-test.properties")
class SaibmApplicationTests {

	private static final String FIXTURE_TITLE = "WP-00-A deterministic fixture";

	@Container
	@ServiceConnection
	static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0.42");

	@Autowired
	private JdbcConnectionDetails jdbcConnectionDetails;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void contextUsesThePinnedMySqlContainer() throws SQLException {
		assertThat(MYSQL.isRunning()).isTrue();
		assertThat(jdbcConnectionDetails.getJdbcUrl()).isEqualTo(MYSQL.getJdbcUrl());
		assertThat(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).isEqualTo(1);

		try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
			assertThat(connection.getMetaData().getDatabaseProductName()).isEqualTo("MySQL");
		}

		assertThat(jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM information_schema.tables "
						+ "WHERE table_schema = DATABASE() AND table_name = 'libros'",
				Integer.class)).isEqualTo(1);
		assertThat(jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM libros WHERE nombre_libro = ?",
				Integer.class, FIXTURE_TITLE)).isEqualTo(1);
		assertThat(jdbcTemplate.queryForObject(
				"SELECT stock FROM libros WHERE nombre_libro = ?",
				Integer.class, FIXTURE_TITLE)).isEqualTo(4);
	}

	@Test
	void contextPreservesTheFixtureInvariantAcrossASecondQueryPath() {
		assertThat(jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM libros WHERE stock BETWEEN 0 AND 4",
				Integer.class)).isEqualTo(1);
		assertThat(jdbcTemplate.queryForObject(
				"SELECT autor FROM libros WHERE nombre_libro = ? AND stock > 0",
				String.class, FIXTURE_TITLE)).isEqualTo("SAIBM");
	}

}
