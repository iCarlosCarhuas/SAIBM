package edu.pe.cibertec.saibm.catalog.book;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;

import edu.pe.cibertec.saibm.catalog.inventory.InventoryEntity;
import edu.pe.cibertec.saibm.catalog.inventory.InventoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookWriteServiceTest {
    @Mock
    private BookRepository books;
    @Mock
    private InventoryRepository inventories;

    @Test
    void createStoresBookAndOwnsInitialInventory() {
        when(books.save(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation ->
                new BookEntity(9, "Java", "", "Ada", "", true));
        BookWriteService service = new BookWriteService(books, inventories);

        BookResponse result = service.create(new BookCreateRequest("Java", "", "Ada", "", 4));

        assertThat(result.id()).isEqualTo(9);
        org.mockito.Mockito.verify(inventories).save(new InventoryEntity(9, 4, 4));
    }

    @Test
    void deleteDeactivatesBookWithoutRemovingIt() {
        BookEntity book = new BookEntity(9, "Java", "", "Ada", "", true);
        when(books.findById(9)).thenReturn(Optional.of(book));
        new BookWriteService(books, inventories).delete(9);

        assertThat(book.isActive()).isFalse();
        org.mockito.Mockito.verify(books).save(book);
    }

    @Test
    void updateCanIncreaseStockWithoutChangingReservedQuantity() {
        BookEntity book = new BookEntity(9, "Java", "", "Ada", "", true);
        when(books.findById(9)).thenReturn(Optional.of(book));
        when(inventories.findByBookId(9)).thenReturn(Optional.of(new InventoryEntity(9, 5, 3)));

        BookResponse result = new BookWriteService(books, inventories).update(9,
                new BookUpdateRequest("Java 2", "", "Ada", "", 7));

        assertThat(result.totalStock()).isEqualTo(7);
        assertThat(result.availableStock()).isEqualTo(5);
    }
}
