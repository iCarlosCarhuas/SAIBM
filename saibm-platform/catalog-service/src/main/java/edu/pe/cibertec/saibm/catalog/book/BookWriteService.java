package edu.pe.cibertec.saibm.catalog.book;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.pe.cibertec.saibm.catalog.inventory.InventoryEntity;
import edu.pe.cibertec.saibm.catalog.inventory.InventoryRepository;

@Service
public class BookWriteService {
    private final BookRepository books;
    private final InventoryRepository inventories;

    public BookWriteService(BookRepository books, InventoryRepository inventories) {
        this.books = books;
        this.inventories = inventories;
    }

    @Transactional
    public BookResponse create(BookCreateRequest request) {
        int stock = request.initialStock() == null ? 0 : request.initialStock();
        BookEntity book = books.save(new BookEntity(null, request.title().trim(), request.description(),
                request.author().trim(), request.imageUrl(), true));
        inventories.save(new InventoryEntity(book.getId(), stock, stock));
        return response(book, stock, stock);
    }

    @Transactional
    public BookResponse update(Integer id, BookUpdateRequest request) {
        BookEntity book = books.findById(id).orElseThrow(() -> new BookNotFoundException(id));
        book.update(request.title().trim(), request.description(), request.author().trim(), request.imageUrl());
        InventoryEntity inventory = inventories.findByBookId(id).orElseGet(() ->
                new InventoryEntity(id, request.totalStock() == null ? 0 : request.totalStock(),
                        request.totalStock() == null ? 0 : request.totalStock()));
        if (request.totalStock() != null) inventory.adjustTotal(request.totalStock());
        books.save(book);
        inventories.save(inventory);
        return response(book, inventory.getTotalStock(), inventory.getAvailableStock());
    }

    @Transactional
    public void delete(Integer id) {
        BookEntity book = books.findById(id).orElseThrow(() -> new BookNotFoundException(id));
        book.deactivate();
        books.save(book);
    }

    private BookResponse response(BookEntity book, int total, int available) {
        return new BookResponse(book.getId(), book.getTitle(), book.getDescription(), book.getAuthor(),
                book.getImageUrl(), total, available);
    }
}
