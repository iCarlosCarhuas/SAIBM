package edu.pe.cibertec.saibm.usuario;

import static org.assertj.core.api.Assertions.*;

import java.nio.file.*;

import org.junit.jupiter.api.Test;

class InfrastructureContractTest {

    @Test
    void boundaryOwnsPiiSchemaAndKeepsAuthenticationOut() throws Exception {
        var root = Path.of("src/main");
        try (var files = Files.walk(root.resolve("java/edu/pe/cibertec/saibm/usuario/domain"))) {
            files.filter(path -> path.toString().endsWith(".java")).forEach(path -> {
                try {
                    assertThat(Files.readString(path)).doesNotContain("org.springframework", "jakarta.persistence");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
        assertThat(Files.readString(root.resolve("java/edu/pe/cibertec/saibm/usuario/application/port/in/UserUseCase.java")))
                .doesNotContain("port.out");
        var sql = Files.readString(root.resolve("resources/db/migration/V1__usuario.sql"));
        assertThat(sql).contains("user_profiles", "user_outbox", "legacy_user_id")
                .doesNotContain("contraseña", "password", "rol_id", "membresia_id", "references");
        var yaml = Files.readString(root.resolve("resources/application.yml"));
        assertThat(yaml).contains("name: usuario-service", "ddl-auto: validate", "flyway:", "probes:",
                "backfill:", "enabled: ${SAIBM_USUARIO_BACKFILL_ENABLED:false}");
        assertThat(Files.readString(root.resolve("java/edu/pe/cibertec/saibm/usuario/domain/event/UserEventType.java")))
                .contains("UserProfileCreated.v1", "UserProfileChanged.v1", "UserDeactivated.v1");
        assertThat(Files.readString(Path.of("Dockerfile"))).contains("EXPOSE 8088", "USER saibm");
        var publisher = Files.readString(root.resolve(
                "java/edu/pe/cibertec/saibm/usuario/infrastructure/messaging/UserOutboxPublisher.java"));
        assertThat(publisher).contains("publisher-enabled", "havingValue = \"true\"")
                .doesNotContain(".published()", "outbox.save(");
    }
}
