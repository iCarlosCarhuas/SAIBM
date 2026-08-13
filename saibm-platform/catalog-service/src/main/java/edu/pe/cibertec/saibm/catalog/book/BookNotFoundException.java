package edu.pe.cibertec.saibm.catalog.book;

public class BookNotFoundException extends RuntimeException {
    public BookNotFoundException(Integer id) {
        super("Active book not found: " + id);
    }
}
