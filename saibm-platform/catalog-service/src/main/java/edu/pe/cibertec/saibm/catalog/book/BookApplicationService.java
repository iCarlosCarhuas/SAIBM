package edu.pe.cibertec.saibm.catalog.book;

public interface BookApplicationService {
    PageResponse<BookResponse> search(String query, int page, int size);
    BookResponse findActive(Integer id);
}
