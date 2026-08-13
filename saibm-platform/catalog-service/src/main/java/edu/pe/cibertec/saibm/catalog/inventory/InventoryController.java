package edu.pe.cibertec.saibm.catalog.inventory;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventory/{bookId}")
public class InventoryController {
    private final InventoryService service;

    public InventoryController(InventoryService service) {
        this.service = service;
    }

    @PostMapping("/hold")
    public InventoryResponse hold(@PathVariable Integer bookId, @RequestHeader(value = "Idempotency-Key", required = false) String key,
            @Valid @RequestBody InventoryRequest request) {
        requireKey(key);
        return service.hold(bookId, request, key);
    }

    @PostMapping("/commit")
    public InventoryResponse commit(@PathVariable Integer bookId, @RequestHeader(value = "Idempotency-Key", required = false) String key) {
        requireKey(key);
        return service.commit(bookId, key);
    }

    @PostMapping("/release")
    public InventoryResponse release(@PathVariable Integer bookId, @RequestHeader(value = "Idempotency-Key", required = false) String key) {
        requireKey(key);
        return service.release(bookId, key);
    }

    private void requireKey(String key) {
        if (key == null || key.isBlank() || key.length() > 200) {
            throw new edu.pe.cibertec.saibm.catalog.book.InvalidRequestException("Idempotency-Key is required");
        }
    }
}
