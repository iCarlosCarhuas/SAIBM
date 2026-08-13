package edu.pe.cibertec.saibm.catalog.migration;

public record CatalogBookSnapshot(Integer id, String title, String description, String author, String imageUrl, int stock) {
}
