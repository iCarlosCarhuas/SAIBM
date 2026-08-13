package edu.pe.cibertec.saibm.catalog.inventory;

import java.time.Instant;

public class InventoryStateMachine {
    public InventoryState transition(InventoryState current, InventoryOperation operation, Instant expiresAt) {
        if (current == InventoryState.HELD && Instant.now().isAfter(expiresAt)) {
            if (operation == InventoryOperation.COMMIT) throw new InventoryConflictException("Expired hold cannot be committed");
            return InventoryState.EXPIRED;
        }
        if (current != InventoryState.HELD) {
            throw new InventoryConflictException("Only a held operation can transition");
        }
        return operation == InventoryOperation.COMMIT ? InventoryState.COMMITTED : InventoryState.RELEASED;
    }
}
