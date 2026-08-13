package edu.pe.cibertec.saibm.catalog.book;

public record BookResponse(Integer id, String title, String description, String author, String imageUrl,
        Integer totalStock, Integer availableStock) {
}
