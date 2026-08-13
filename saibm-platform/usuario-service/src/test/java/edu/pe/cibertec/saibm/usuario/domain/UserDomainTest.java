package edu.pe.cibertec.saibm.usuario.domain;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import edu.pe.cibertec.saibm.usuario.domain.model.AccountStatus;
import edu.pe.cibertec.saibm.usuario.domain.model.UserProfile;

class UserDomainTest {

    @Test
    void profileNormalizesPiiWithoutAuthenticationFields() {
        var profile = UserProfile.create(UUID.randomUUID(), " Ana ", " Torres ", "12345678",
                "ANA@EXAMPLE.COM", "42", Instant.parse("2030-01-01T00:00:00Z"));

        assertThat(profile.firstName()).isEqualTo("Ana");
        assertThat(profile.lastName()).isEqualTo("Torres");
        assertThat(profile.email()).isEqualTo("ana@example.com");
        assertThat(profile.status()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(profile.legacyUserId()).isEqualTo("42");
    }

    @Test
    void lifecycleChangesAreExplicitAndReversible() {
        var profile = UserProfile.create(UUID.randomUUID(), "Ana", "Torres", "12345678",
                "ana@example.com", null, Instant.now());

        assertThat(profile.deactivate(Instant.now()).status()).isEqualTo(AccountStatus.DEACTIVATED);
        assertThat(profile.deactivate(Instant.now()).active()).isFalse();
        assertThat(profile.deactivate(Instant.now()).reactivate(Instant.now()).active()).isTrue();
    }

    @Test
    void invalidPiiIsRejectedByTheDomain() {
        assertThatThrownBy(() -> UserProfile.create(UUID.randomUUID(), "", "Torres", "123",
                "not-an-email", null, Instant.now())).isInstanceOf(IllegalArgumentException.class);
    }
}
