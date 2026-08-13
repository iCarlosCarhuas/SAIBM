package edu.pe.cibertec.saibm.catalog.inventory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "inventory")
public class InventoryEntity {
    @Id
    @Column(name = "book_id")
    private Integer bookId;
    @Column(name = "total_stock", nullable = false)
    private int totalStock;
    @Column(name = "available_stock", nullable = false)
    private int availableStock;

    protected InventoryEntity() {
    }

    public InventoryEntity(Integer bookId, int totalStock, int availableStock) {
        this.bookId = bookId;
        this.totalStock = totalStock;
        this.availableStock = availableStock;
    }

    public Integer getBookId() { return bookId; }
    public int getTotalStock() { return totalStock; }
    public int getAvailableStock() { return availableStock; }
    public boolean reserve(int quantity) {
        if (quantity < 1 || availableStock < quantity) return false;
        availableStock -= quantity;
        return true;
    }
    public void release(int quantity) { availableStock = Math.min(totalStock, availableStock + quantity); }

    public void adjustTotal(int newTotal) {
        int reserved = totalStock - availableStock;
        if (newTotal < reserved) {
            throw new InventoryConflictException("Total stock cannot be lower than reserved stock");
        }
        totalStock = newTotal;
        availableStock = newTotal - reserved;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof InventoryEntity expected)) return false;
        return java.util.Objects.equals(bookId, expected.bookId)
                && totalStock == expected.totalStock && availableStock == expected.availableStock;
    }

    @Override
    public int hashCode() { return java.util.Objects.hash(bookId, totalStock, availableStock); }
}
