package edu.pe.cibertec.saibm.catalog.book;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import edu.pe.cibertec.saibm.catalog.inventory.InventoryEntity;
import edu.pe.cibertec.saibm.catalog.inventory.InventoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

@ExtendWith(MockitoExtension.class)
class BookReadServiceTest {
    @Mock
    private BookRepository books;
    @Mock
    private InventoryRepository inventories;

    @Test
    void searchIncludesOwnedInventoryInThePublicDto() {
        BookEntity book = new BookEntity(7, "Java", "", "Ada", "", true);
        when(books.searchActive(org.mockito.ArgumentMatchers.eq("java"), org.mockito.ArgumentMatchers.any()))
                .thenReturn(new PageImpl<>(List.of(book)));
        when(inventories.findById(7)).thenReturn(java.util.Optional.of(new InventoryEntity(7, 5, 3)));

        PageResponse<BookResponse> result = new BookService(books, inventories).search(" java ", 0, 20);

        assertThat(result.content().get(0).totalStock()).isEqualTo(5);
        assertThat(result.content().get(0).availableStock()).isEqualTo(3);
    }
}
