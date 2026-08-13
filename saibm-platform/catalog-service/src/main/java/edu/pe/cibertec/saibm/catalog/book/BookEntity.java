package edu.pe.cibertec.saibm.catalog.book;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "books")
public class BookEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false, length = 255)
    private String title;
    @Column(nullable = false, columnDefinition = "text")
    private String description;
    @Column(nullable = false, length = 255)
    private String author;
    @Column(name = "image_url", nullable = false, length = 300)
    private String imageUrl;
    @Column(nullable = false)
    private boolean active;

    protected BookEntity() {
    }

    public BookEntity(Integer id, String title, String description, String author, String imageUrl, boolean active) {
        this.id = id;
        this.title = title;
        this.description = description == null ? "" : description;
        this.author = author;
        this.imageUrl = imageUrl == null ? "" : imageUrl;
        this.active = active;
    }

    public Integer getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getAuthor() { return author; }
    public String getImageUrl() { return imageUrl; }
    public boolean isActive() { return active; }
    public void update(String title, String description, String author, String imageUrl) {
        this.title = title;
        this.description = description == null ? "" : description;
        this.author = author;
        this.imageUrl = imageUrl == null ? "" : imageUrl;
    }
    public void deactivate() { this.active = false; }
}
