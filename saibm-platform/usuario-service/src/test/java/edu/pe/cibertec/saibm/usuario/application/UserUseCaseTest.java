package edu.pe.cibertec.saibm.usuario.application;

import static org.assertj.core.api.Assertions.*;

import java.time.*;
import java.util.*;

import org.junit.jupiter.api.Test;

import edu.pe.cibertec.saibm.usuario.application.port.in.UserUseCase;
import edu.pe.cibertec.saibm.usuario.application.port.out.*;
import edu.pe.cibertec.saibm.usuario.application.usecase.UserService;
import edu.pe.cibertec.saibm.usuario.domain.event.UserEvent;
import edu.pe.cibertec.saibm.usuario.domain.model.UserProfile;
import edu.pe.cibertec.saibm.usuario.domain.exception.UserNotFoundException;

class UserUseCaseTest {

    private final Store store = new Store();
    private final List<UserEvent> events = new ArrayList<>();
    private final UserService use = new UserService(store, events::add,
            Clock.fixed(Instant.parse("2030-01-01T00:00:00Z"), ZoneOffset.UTC));

    @Test
    void profileCrudPublishesEventsAndPreservesExplicitFields() {
        var created = use.create(command("Ana", "Torres", "12345678", "ana@example.com"), "c1");
        var changed = use.update(created.id(), command("Ana Maria", "Torres", "12345678", "ana@example.com"),
                new UserUseCase.Actor(created.id().toString(), false), "c2");

        assertThat(changed.firstName()).isEqualTo("Ana Maria");
        assertThat(changed.active()).isTrue();
        assertThat(events).extracting(UserEvent::eventType)
                .containsExactly("UserProfileCreated.v1", "UserProfileChanged.v1");
    }

    @Test
    void foreignActorCannotReadOrChangeProfileButAdminCanList() {
        var created = use.create(command("Ana", "Torres", "12345678", "ana@example.com"), "c1");

        assertThatThrownBy(() -> use.find(created.id(), new UserUseCase.Actor("other", false)))
                .isInstanceOf(UserNotFoundException.class);
        assertThat(use.list("ana", new UserUseCase.Actor("admin", true))).hasSize(1);
        assertThatThrownBy(() -> use.list("", new UserUseCase.Actor("other", false)))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void accountLifecycleEmitsDeactivationAndHidesInactiveProfileFromOwner() {
        var created = use.create(command("Ana", "Torres", "12345678", "ana@example.com"), "c1");

        use.deactivate(created.id(), new UserUseCase.Actor("admin", true), "c3");

        assertThat(use.find(created.id(), new UserUseCase.Actor("admin", true)).active()).isFalse();
        assertThat(events).extracting(UserEvent::eventType).contains("UserDeactivated.v1");
    }

    private UserUseCase.UserCommand command(String firstName, String lastName, String dni, String email) {
        return new UserUseCase.UserCommand(firstName, lastName, dni, email);
    }

    static final class Store implements UserStorePort {
        final Map<UUID, UserProfile> profiles = new LinkedHashMap<>();

        public Optional<UserProfile> find(UUID id) {
            return Optional.ofNullable(profiles.get(id));
        }

        public List<UserProfile> search(String query) {
            var q = query.toLowerCase(Locale.ROOT);
            return profiles.values().stream().filter(p -> q.isBlank()
                    || (p.firstName() + " " + p.lastName() + " " + p.email()).toLowerCase(Locale.ROOT).contains(q))
                    .toList();
        }

        public void save(UserProfile profile) {
            profiles.put(profile.id(), profile);
        }
    }
}
