package edu.pe.cibertec.saibm.usuario.persistence;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.*;
import org.testcontainers.postgresql.PostgreSQLContainer;

import edu.pe.cibertec.saibm.usuario.application.port.in.*;
import edu.pe.cibertec.saibm.usuario.infrastructure.config.*;
import edu.pe.cibertec.saibm.usuario.infrastructure.persistence.*;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@Testcontainers
@Import({ UserPersistenceAdapter.class, UserConfig.class })
class UserPostgresTest {

    @Container static final PostgreSQLContainer PG = new PostgreSQLContainer("postgres:16.9-alpine");

    @DynamicPropertySource
    static void db(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", PG::getJdbcUrl);
        registry.add("spring.datasource.username", PG::getUsername);
        registry.add("spring.datasource.password", PG::getPassword);
    }

    @Autowired JdbcTemplate sql;
    @Autowired @Qualifier("users") UserUseCase use;

    @BeforeEach
    void clean() {
        sql.update("delete from user_outbox");
        sql.update("delete from user_profiles");
    }

    @Test
    void profilePersistsPiiAliasAndLifecycleWithoutAuthTables() {
        var created = use.create(new UserUseCase.UserCommand("Ana", "Torres", "12345678",
                "ana@example.com"), "corr-1");
        use.deactivate(created.id(), new UserUseCase.Actor("admin", true), "corr-2");

        assertThat(sql.queryForObject("select first_name from user_profiles", String.class)).isEqualTo("Ana");
        assertThat(sql.queryForObject("select legacy_user_id from user_profiles", String.class)).isNull();
        assertThat(sql.queryForObject("select status from user_profiles", String.class)).isEqualTo("DEACTIVATED");
        assertThat(sql.queryForObject("select count(*) from user_outbox", Integer.class)).isEqualTo(2);
    }
}
