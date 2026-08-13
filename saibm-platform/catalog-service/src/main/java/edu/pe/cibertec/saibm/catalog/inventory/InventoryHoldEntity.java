package edu.pe.cibertec.saibm.catalog.inventory;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "inventory_holds")
public class InventoryHoldEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "operation_key", nullable = false, unique = true, length = 200)
    private String operationKey;
    @Column(name = "book_id", nullable = false)
    private Integer bookId;
    @Column(nullable = false)
    private int quantity;
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private InventoryState state;

    protected InventoryHoldEntity() {
    }

    public static InventoryHoldEntity held(Long id, String key, int bookId, int quantity, Instant expiresAt, String hash) {
        InventoryHoldEntity entity = new InventoryHoldEntity();
        entity.id = id;
        entity.operationKey = key;
        entity.bookId = bookId;
        entity.quantity = quantity;
        entity.expiresAt = expiresAt;
        entity.requestHash = hash;
        entity.state = InventoryState.HELD;
        return entity;
    }

    public InventoryHoldEntity(String key, int bookId, int quantity, Instant expiresAt, String hash) {
        this.operationKey = key;
        this.bookId = bookId;
        this.quantity = quantity;
        this.expiresAt = expiresAt;
        this.requestHash = hash;
        this.state = InventoryState.HELD;
    }

    public Long getId() { return id; }
    public String getOperationKey() { return operationKey; }
    public Integer getBookId() { return bookId; }
    public int getQuantity() { return quantity; }
    public Instant getExpiresAt() { return expiresAt; }
    public String getRequestHash() { return requestHash; }
    public InventoryState getState() { return state; }
    public void transition(InventoryState state) { this.state = state; }
}
