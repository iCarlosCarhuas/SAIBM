package edu.pe.cibertec.saibm.catalog.inventory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryServiceImpl implements InventoryService {
    private final InventoryRepository inventories;
    private final InventoryHoldRepository holds;
    private final RequestHasher hasher;

    public InventoryServiceImpl(InventoryRepository inventories, InventoryHoldRepository holds, RequestHasher hasher) {
        this.inventories = inventories;
        this.holds = holds;
        this.hasher = hasher;
    }

    @Override
    @Transactional
    public InventoryResponse hold(Integer bookId, InventoryRequest request, String key) {
        String hash = hasher.hold(bookId, request.quantity(), request.expiresAt().toString());
        var existing = holds.findByOperationKeyForUpdate(key);
        if (existing.isPresent()) return replay(existing.get(), hash, bookId);
        if (holds.insertIfAbsent(key, bookId, request.quantity(), request.expiresAt(), hash) == 0) {
            return holds.findByOperationKeyForUpdate(key)
                    .map(hold -> replay(hold, hash, bookId))
                    .orElseThrow(() -> new InventoryConflictException("Idempotency operation was not stored"));
        }
        InventoryHoldEntity hold = holds.findByOperationKeyForUpdate(key)
                .orElseThrow(() -> new InventoryConflictException("Inventory operation was not stored"));
        InventoryEntity inventory = inventories.findByBookId(bookId)
                .orElseThrow(() -> new InventoryConflictException("Inventory not found"));
        if (!inventory.reserve(request.quantity())) throw new InventoryConflictException("Insufficient stock");
        inventories.saveAndFlush(inventory);
        return response(hold);
    }

    @Override
    @Transactional
    public InventoryResponse commit(Integer bookId, String key) {
        return transition(bookId, key, InventoryOperation.COMMIT);
    }

    @Override
    @Transactional
    public InventoryResponse release(Integer bookId, String key) {
        return transition(bookId, key, InventoryOperation.RELEASE);
    }

    private InventoryResponse transition(Integer bookId, String key, InventoryOperation operation) {
        InventoryHoldEntity hold = holds.findByOperationKeyForUpdate(key)
                .orElseThrow(() -> new InventoryConflictException("Inventory operation not found"));
        if (!hold.getBookId().equals(bookId)) throw new InventoryConflictException("Operation book mismatch");
        if (hold.getState() == (operation == InventoryOperation.COMMIT ? InventoryState.COMMITTED : InventoryState.RELEASED)) {
            return response(hold);
        }
        InventoryState next = new InventoryStateMachine().transition(hold.getState(), operation, hold.getExpiresAt());
        if (next == InventoryState.EXPIRED) {
            hold.transition(next);
            inventories.findByBookId(bookId).ifPresent(i -> i.release(hold.getQuantity()));
        } else if (next == InventoryState.RELEASED) {
            inventories.findByBookId(bookId).ifPresent(i -> i.release(hold.getQuantity()));
            hold.transition(next);
        } else {
            hold.transition(next);
        }
        return response(hold);
    }

    private InventoryResponse replay(InventoryHoldEntity existing, String hash, Integer bookId) {
        if (!existing.getRequestHash().equals(hash) || !existing.getBookId().equals(bookId)) {
            throw new InventoryConflictException("Idempotency key was reused with a different payload");
        }
        return response(existing);
    }

    private InventoryResponse response(InventoryHoldEntity hold) {
        return new InventoryResponse(hold.getId(), hold.getOperationKey(), hold.getBookId(), hold.getQuantity(),
                hold.getState(), hold.getExpiresAt());
    }
}
