package edu.pe.cibertec.saibm.catalog.migration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
@ConditionalOnProperty(name = "catalog.backfill.enabled", havingValue = "true")
public class BackfillConfiguration {
    @Bean
    JdbcTemplate legacyJdbcTemplate(@Value("${legacy.datasource.url:}") String url,
            @Value("${legacy.datasource.username:}") String username,
            @Value("${legacy.datasource.password:}") String password) {
        if (url.isBlank() || username.isBlank()) {
            throw new IllegalStateException("Legacy backfill datasource is not configured");
        }
        DriverManagerDataSource dataSource = new DriverManagerDataSource(url, username, password);
        return new JdbcTemplate(dataSource);
    }

    @Bean
    LegacyBookReader legacyBookReader(@Qualifier("legacyJdbcTemplate") JdbcTemplate jdbcTemplate) {
        return new LegacyBookReader(jdbcTemplate);
    }
}
