package edu.pe.cibertec.saibm.catalog.inventory;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RequestHasherTest {
    @Test
    void canonicalHashIsStableAndChangesWhenPayloadChanges() {
        RequestHasher hasher = new RequestHasher();

        String first = hasher.hold(7, 2, "2030-01-01T00:00:00Z");
        String replay = hasher.hold(7, 2, "2030-01-01T00:00:00Z");
        String changed = hasher.hold(7, 3, "2030-01-01T00:00:00Z");

        assertThat(first).isEqualTo(replay).hasSize(64);
        assertThat(changed).isNotEqualTo(first);
    }
}
