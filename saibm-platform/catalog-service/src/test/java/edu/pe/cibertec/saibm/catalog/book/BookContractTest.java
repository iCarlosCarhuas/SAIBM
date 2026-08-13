package edu.pe.cibertec.saibm.catalog.book;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Modifier;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

class BookContractTest {

    @Test
    void bookResponseIsAnExplicitDtoWithInventoryFields() {
        assertThat(BookResponse.class.isRecord()).isTrue();
        assertThat(Arrays.stream(BookResponse.class.getRecordComponents()).map(component -> component.getName()))
                .containsExactly("id", "title", "description", "author", "imageUrl", "totalStock", "availableStock");
        assertThat(Modifier.isPublic(BookResponse.class.getModifiers())).isTrue();
    }

    @Test
    void pageResponseKeepsStableMetadataSeparateFromPersistence() {
        PageResponse<BookResponse> page = new PageResponse<>(
                java.util.List.of(new BookResponse(7, "Java", "", "Ada", "", 4, 3)), 1, 1, 4, 4);

        assertThat(page.content()).hasSize(1);
        assertThat(page.page()).isEqualTo(1);
        assertThat(page.size()).isEqualTo(1);
        assertThat(page.totalElements()).isEqualTo(4);
        assertThat(page.totalPages()).isEqualTo(4);
        assertThat(page.hasNext()).isTrue();
        assertThat(page.hasPrevious()).isTrue();
    }
}
