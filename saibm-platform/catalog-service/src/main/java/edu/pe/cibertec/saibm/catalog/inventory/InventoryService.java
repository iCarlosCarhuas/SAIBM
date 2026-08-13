package edu.pe.cibertec.saibm.catalog.inventory;

public interface InventoryService {
    InventoryResponse hold(Integer bookId, InventoryRequest request, String key);
    InventoryResponse commit(Integer bookId, String key);
    InventoryResponse release(Integer bookId, String key);
}
