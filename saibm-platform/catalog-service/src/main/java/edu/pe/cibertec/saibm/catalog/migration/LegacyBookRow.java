package edu.pe.cibertec.saibm.catalog.migration;

public record LegacyBookRow(Integer id, String title, String description, Integer stock, String author, String imageUrl) {
}
