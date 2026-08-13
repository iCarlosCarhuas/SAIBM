package edu.pe.cibertec.saibm.catalog.book;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BookCreateRequest(
        @NotBlank @Size(max = 255) String title,
        @Size(max = 4000) String description,
        @NotBlank @Size(max = 255) String author,
        @Size(max = 300) String imageUrl,
        @Min(0) Integer initialStock) {
}
