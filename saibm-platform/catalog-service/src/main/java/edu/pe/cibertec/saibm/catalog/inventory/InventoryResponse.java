package edu.pe.cibertec.saibm.catalog.inventory;

import java.time.Instant;

public record InventoryResponse(Long operationId, String operationKey, Integer bookId, int quantity,
        InventoryState state, Instant expiresAt) {
}
