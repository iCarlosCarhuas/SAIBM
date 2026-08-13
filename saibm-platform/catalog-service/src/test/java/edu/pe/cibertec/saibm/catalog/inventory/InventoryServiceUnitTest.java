package edu.pe.cibertec.saibm.catalog.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InventoryServiceUnitTest {
    @Mock
    private InventoryRepository inventories;
    @Mock
    private InventoryHoldRepository holds;

    @Test
    void identicalHoldReplayReturnsStoredResultWithoutSecondReservation() {
        Instant expiry = Instant.parse("2030-01-01T00:00:00Z");
        InventoryHoldEntity existing = InventoryHoldEntity.held(11L, "same", 7, 2,
                expiry, new RequestHasher().hold(7, 2, expiry.toString()));
        when(holds.findByOperationKeyForUpdate("same")).thenReturn(Optional.of(existing));

        InventoryService service = new InventoryServiceImpl(inventories, holds, new RequestHasher());
        InventoryResponse response = service.hold(7, new InventoryRequest(2, expiry), "same");

        assertThat(response.state()).isEqualTo(InventoryState.HELD);
        org.mockito.Mockito.verifyNoInteractions(inventories);
    }

    @Test
    void reusedKeyWithDifferentPayloadIsAConflict() {
        Instant expiry = Instant.parse("2030-01-01T00:00:00Z");
        InventoryHoldEntity existing = InventoryHoldEntity.held(11L, "same", 7, 2,
                expiry, new RequestHasher().hold(7, 2, expiry.toString()));
        when(holds.findByOperationKeyForUpdate("same")).thenReturn(Optional.of(existing));

        InventoryService service = new InventoryServiceImpl(inventories, holds, new RequestHasher());

        assertThatThrownBy(() -> service.hold(7, new InventoryRequest(3, expiry), "same"))
                .isInstanceOf(InventoryConflictException.class);
    }
}
