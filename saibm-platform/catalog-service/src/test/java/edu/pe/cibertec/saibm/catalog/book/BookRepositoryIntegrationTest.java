package edu.pe.cibertec.saibm.catalog.book;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@Testcontainers
class BookRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16.9-alpine");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.flyway.enabled", () -> true);
    }

    @org.springframework.beans.factory.annotation.Autowired
    private BookRepository books;

    @Test
    void flywaySchemaSearchesActiveBooksWithStableTitleAndIdOrder() {
        books.save(new BookEntity(null, "Java Patterns", "", "Ada", "", true));
        books.save(new BookEntity(null, "Java Security", "", "Grace", "", true));
        books.save(new BookEntity(null, "Hidden Java", "", "Alan", "", false));

        var result = books.searchActive("java", PageRequest.of(0, 10));

        assertThat(result.getContent()).extracting(BookEntity::getTitle)
                .containsExactly("Java Patterns", "Java Security");
        assertThat(result.getContent()).allMatch(BookEntity::isActive);
    }
}
