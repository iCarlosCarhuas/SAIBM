package edu.pe.cibertec.saibm.catalog.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class InventoryStateMachineTest {
    private final InventoryStateMachine machine = new InventoryStateMachine();

    @Test
    void heldHoldCanCommitOrReleaseExactlyOnce() {
        assertThat(machine.transition(InventoryState.HELD, InventoryOperation.COMMIT, Instant.MAX))
                .isEqualTo(InventoryState.COMMITTED);
        assertThat(machine.transition(InventoryState.HELD, InventoryOperation.RELEASE, Instant.MAX))
                .isEqualTo(InventoryState.RELEASED);
    }

    @Test
    void expiredOrFinalHoldCannotBeCommitted() {
        assertThatThrownBy(() -> machine.transition(InventoryState.HELD, InventoryOperation.COMMIT, Instant.MIN))
                .isInstanceOf(InventoryConflictException.class);
        assertThat(machine.transition(InventoryState.HELD, InventoryOperation.RELEASE, Instant.MIN))
                .isEqualTo(InventoryState.EXPIRED);
        assertThatThrownBy(() -> machine.transition(InventoryState.RELEASED, InventoryOperation.COMMIT, Instant.MAX))
                .isInstanceOf(InventoryConflictException.class);
    }
}
