package edu.pe.cibertec.saibm.catalog.inventory;

import java.time.Instant;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record InventoryRequest(@Min(1) int quantity, @NotNull @Future Instant expiresAt) {
}
