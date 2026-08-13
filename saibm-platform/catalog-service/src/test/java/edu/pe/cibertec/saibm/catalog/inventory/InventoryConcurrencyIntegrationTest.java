package edu.pe.cibertec.saibm.catalog.inventory;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import edu.pe.cibertec.saibm.catalog.book.BookEntity;
import edu.pe.cibertec.saibm.catalog.book.BookRepository;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@Import({InventoryServiceImpl.class, RequestHasher.class})
@Testcontainers
@org.springframework.transaction.annotation.Transactional(
        propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
class InventoryConcurrencyIntegrationTest {
    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16.9-alpine");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.flyway.enabled", () -> true);
    }

    @org.springframework.beans.factory.annotation.Autowired
    private InventoryRepository inventories;
    @org.springframework.beans.factory.annotation.Autowired
    private BookRepository books;
    @org.springframework.beans.factory.annotation.Autowired
    private InventoryHoldRepository holds;
    @org.springframework.beans.factory.annotation.Autowired
    private InventoryService service;

    @Test
    void concurrentHoldsHaveExactlyOneWinnerAndNeverNegativeStock() throws Exception {
        // The row-level lock is exercised by real PostgreSQL transactions in parallel.
        BookEntity book = books.saveAndFlush(new BookEntity(null, "Concurrency", "", "Test", "", true));
        int bookId = book.getId();
        inventories.saveAndFlush(new InventoryEntity(bookId, 1, 1));
        var pool = Executors.newFixedThreadPool(2);
        try {
            var results = pool.invokeAll(IntStream.range(0, 2).mapToObj(index -> (Callable<Boolean>) () -> {
                try {
                    service.hold(bookId, new InventoryRequest(1, Instant.now().plusSeconds(60)), "key-" + index);
                    return true;
                } catch (InventoryConflictException exception) {
                    return false;
                }
            }).toList());
            long winners = 0;
            for (var result : results) {
                if (result.get()) winners++;
            }
            assertThat(winners).isEqualTo(1);
            assertThat(inventories.findById(bookId).orElseThrow().getAvailableStock()).isZero();
        } finally {
            pool.shutdownNow();
        }
    }
}
