package edu.pe.cibertec.saibm.usuario.migration;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import edu.pe.cibertec.saibm.usuario.infrastructure.migration.*;

class UserBackfillTest {

    @Test
    void mapperPreservesLegacyIdentityWhileDroppingAuthAndRoleFields() {
        var row = new LegacyUserRow(7L, "Ana", "Torres", "12345678", "ana@example.com");
        var profile = UserBackfillMapper.map(row);

        assertThat(profile.legacyUserId()).isEqualTo("7");
        assertThat(profile.email()).isEqualTo("ana@example.com");
        assertThat(profile.active()).isTrue();
    }

    @Test
    void reconciliationRejectsMissingOrDuplicateLegacyRows() {
        var row = new LegacyUserRow(7L, "Ana", "Torres", "12345678", "ana@example.com");
        var profile = UserBackfillMapper.map(row);
        var reconciler = new UserBackfillReconciler();

        assertThat(reconciler.reconcile(List.of(row), List.of(profile)).reconciled()).isTrue();
        assertThat(reconciler.reconcile(List.of(row, row), List.of(profile)).reconciled()).isFalse();
    }
}
