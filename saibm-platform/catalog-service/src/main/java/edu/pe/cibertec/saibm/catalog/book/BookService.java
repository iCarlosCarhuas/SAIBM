package edu.pe.cibertec.saibm.catalog.book;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.pe.cibertec.saibm.catalog.inventory.InventoryRepository;

@Service
@Transactional(readOnly = true)
public class BookService implements BookApplicationService {
    private final BookRepository repository;
    private final InventoryRepository inventories;

    public BookService(BookRepository repository, InventoryRepository inventories) {
        this.repository = repository;
        this.inventories = inventories;
    }

    @Override
    public PageResponse<BookResponse> search(String query, int page, int size) {
        var result = repository.searchActive(query == null ? "" : query.trim().toLowerCase(),
                PageRequest.of(page, size));
        return new PageResponse<>(result.getContent().stream().map(this::toResponse).toList(), page, size,
                result.getTotalElements(), result.getTotalPages());
    }

    @Override
    public BookResponse findActive(Integer id) {
        return repository.findByIdAndActiveTrue(id).map(this::toResponse)
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    private BookResponse toResponse(BookEntity book) {
        var inventory = inventories.findById(book.getId());
        return new BookResponse(book.getId(), book.getTitle(), book.getDescription(), book.getAuthor(),
                book.getImageUrl(), inventory.map(i -> i.getTotalStock()).orElse(0),
                inventory.map(i -> i.getAvailableStock()).orElse(0));
    }
}
