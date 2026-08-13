# Despliegue objetivo y transición de SAIBM

Este runbook diseña el estado final de `planning.md`. Sólo los comandos del monolito y los puertos marcados como verificados existen hoy; Docker/Compose, Angular, Flyway, health checks y CI/CD son artefactos objetivo que deben crearse en sus work packages.

## Arquitectura final

| ID | Artefacto objetivo | Exposición | Dependencias |
| --- | --- | --- | --- |
| `SVC-WEB` | Imagen Nginx o servidor estático con `saibm-web/dist`; SPA Angular. | 80/443 por proxy. | `SVC-GATEWAY`. |
| `SVC-GATEWAY` | JAR/imagen Spring Cloud Gateway. | 8080 interno; 80/443 externo vía proxy. | Discovery, IAM y servicios. |
| `SVC-DISCOVERY` | JAR/imagen Eureka Server. | 8761 sólo red administrativa/interna. | Ninguna. |
| `SVC-IAM` | JAR/imagen Spring Boot. | 8081 interno. | `DB-IAM`, reCAPTCHA. |
| `SVC-CATALOG` | JAR/imagen Spring Boot. | 8082 interno. | `DB-CATALOG`. |
| `SVC-CIRC` | JAR/imagen Spring Boot. | 8083 interno. | `DB-CIRC`, Catalog, RabbitMQ. |
| `SVC-REPORT` | JAR/imagen Spring Boot/Jasper. | 8084 interno. | `DB-REPORT`, RabbitMQ. |
| `MQ-01` | RabbitMQ 3 management. | 5672 interno; 15672 sólo administración. | Ninguna. |
| `DB-IAM..REPORT` | PostgreSQL 16, una instancia lógica/BD y usuario por servicio. | 5432 interno; sin publicación en producción. | Volúmenes y backups. |

## Puertos

### Verificados en las referencias

| Sistema/archivo | Puerto verificado |
| --- | ---: |
| SAIBM `application.properties` | Monolito 8000 |
| Marketoditito `eureka-server/application.yml` | Eureka 8761 |
| Marketoditito `api-gateway/application.yml` | Gateway 8080 |
| Marketoditito `auth-service/application.yml` | Auth 8081 |
| Marketoditito `catalog-service/application.yml` | Catalog 8082 |
| Marketoditito `finance-service/application.yml` | Finance 8083 |
| Marketoditito `reporting-worker/application.yml` | Reporting 8084 |
| Marketoditito `docker-compose.yml` | Rabbit 5672/15672; PostgreSQL host 5433/5434/5435 -> 5432 |
| Marketoditito Web/Angular convention | Desarrollo Angular 4200, también permitido por CORS del Gateway |

### Reservados/propuestos para SAIBM

| ID | Puerto | Decisión |
| --- | ---: | --- |
| `PORT-WEB` | 4200 dev; 80/443 release | Angular dev y entrada pública final. |
| `PORT-GATEWAY` | 8080 | Adoptado de referencia; único backend público. |
| `PORT-DISCOVERY` | 8761 | Adoptado, pero interno. |
| `PORT-IAM` | 8081 | Reservado. |
| `PORT-CATALOG` | 8082 | Reservado. |
| `PORT-CIRC` | 8083 | Adaptación del slot finance. |
| `PORT-REPORT` | 8084 | Reservado. |
| `PORT-RABBIT` | 5672/15672 | Interno/admin. |
| `PORT-DB-*` | 5432 interno | Sin host ports en producción; para local se asignarán puertos no conflictivos en `.env.example`. |
| `PORT-MONOLITH` | 8000 temporal | Sólo hasta `DEP-09`. |

## Variables y secretos objetivo

| Componente | Variables no secretas | Secretos |
| --- | --- | --- |
| Todos | `SPRING_PROFILES_ACTIVE`, `SERVER_PORT`, `EUREKA_URL`, `LOG_LEVEL`, `OTEL_*`, versión. | Ninguno compartido por defecto. |
| Gateway | `ALLOWED_ORIGINS`, rutas, issuer/audience, JWKS URL. | Clave/cookie de sesión sólo si BFF; nunca en Angular. |
| IAM | `DB_URL`, `JWT_ISSUER`, `JWT_AUDIENCE`, TTL, reCAPTCHA endpoint/site key. | `DB_PASSWORD`, clave JWT privada/HMAC fuerte, refresh pepper, `RECAPTCHA_SECRET`. |
| Catalog/Circ/Report | `DB_URL`, URLs lógicas, timeout/retry, Rabbit host/vhost. | `DB_PASSWORD`, Rabbit password; credenciales de servicio si aplica. |
| Web | URL pública relativa del Gateway, flags públicos. | **Ninguno.** |

No se copian defaults inseguros de Marketoditito ni valores actuales de SAIBM. Los secretos ya versionados se rotan en `DEP-00`; se entrega sólo `.env.example` sin valores sensibles y el runtime falla cerrado si falta un secreto.

## Dockerfiles y Compose target

- `saibm-platform/<module>/Dockerfile`: build multi-stage Java 21, copiar POMs antes de fuentes para cache, ejecutar la verificación en CI antes de construir, imagen JRE pinneada, usuario no root, `EXPOSE` del puerto interno y sin secretos.
- `saibm-web/Dockerfile`: `pnpm install --frozen-lockfile`, build Angular y etapa estática no root con fallback SPA y headers seguros.
- `compose.yaml`: `discovery`, `gateway`, cuatro servicios, cuatro PostgreSQL, RabbitMQ y web; redes `edge`, `services`, `data`; sólo web/gateway públicos; volúmenes separados; health checks y `depends_on` saludable donde ayude al arranque.
- `.dockerignore` por repo; imágenes con digest/SBOM/scan; Compose no usa passwords literales.
- Estos archivos **no existen hoy en SAIBM** y serán creados por `WP-02`, ampliados por `WP-03..07`.

## Orden de build y arranque final

### Build objetivo

1. CI verifica backend con Maven Wrapper del reactor y frontend con la versión Node/pnpm fijada en `packageManager`/CI.
2. Genera contratos OpenAPI, ejecuta unitarias, integración, contrato, seguridad y E2E.
3. Empaqueta JARs de `SVC-DISCOVERY`, `SVC-GATEWAY`, `SVC-IAM`, `SVC-CATALOG`, `SVC-CIRC`, `SVC-REPORT`.
4. Compila `SVC-WEB`, crea imágenes inmutables, SBOM, firmas y digests.
5. Publica sólo si todas las verificaciones pasan. No se usa `-DskipTests` como prueba de release.

### Arranque objetivo

1. Redes, volúmenes, PostgreSQL y RabbitMQ.
2. Migraciones Flyway de cada propietario con backup/restore verificado.
3. `SVC-DISCOVERY` hasta readiness.
4. `SVC-IAM`, `SVC-CATALOG`, `SVC-CIRC`, `SVC-REPORT` hasta registro y readiness.
5. `SVC-GATEWAY` hasta rutas saludables.
6. `SVC-WEB`/proxy y smoke tests externos.

El orden mejora determinismo, pero cada proceso implementa backoff y no asume que `depends_on` garantiza disponibilidad permanente.

## Bases de datos y migraciones

| ID | Base objetivo | Origen legacy | Propietario |
| --- | --- | --- | --- |
| `DB-IAM` | `saibm_iam` | `usuarios`, `rol`, `perfil`, `acceso`, asignación de `membresia`. | `SVC-IAM` |
| `DB-CATALOG` | `saibm_catalog` | `libros`, stock. | `SVC-CATALOG` |
| `DB-CIRC` | `saibm_circulation` | `reserva`, definición de planes; carrito derivado de sesión si se persiste. | `SVC-CIRC` |
| `DB-REPORT` | `saibm_reporting` | Proyección reconstruible de APIs/eventos; no copia autoritativa. | `SVC-REPORT` |

Cada servicio tiene usuario de mínimo privilegio y su tabla `flyway_schema_history`. Flujo: backup -> esquema expand -> backfill por lotes -> conciliación de conteos/IDs/reglas -> shadow read -> cambio de escritor -> observación -> contract. Se validan encoding, fechas, auto-increment a UUID/mapa, nombres reservados, índices y procedimientos. Los procedimientos MySQL se sustituyen por casos de uso antes del corte. Nunca se revierte una base descartando escrituras aceptadas; se prefiere forward-fix o replay probado.

## Health, observabilidad y smoke

### Health objetivo

- `/actuator/health/liveness`: proceso local, sin fan-out.
- `/actuator/health/readiness`: migraciones locales, DB y capacidad de atender; dependencias con criterios acotados.
- `/actuator/info`, métricas Prometheus y logs JSON con servicio, versión, entorno, correlation/trace ID, ruta, estado y duración.
- Alertas sobre error/latencia/saturación y métricas de dominio: logins fallidos, reservas, rechazos por cupo/stock, holds/compensaciones, eventos pendientes/duplicados y errores PDF.

### Smoke IDs

| ID | Resultado esperado |
| --- | --- |
| `SMK-01` | Discovery registra cuatro servicios y Gateway resuelve todas las rutas. |
| `SMK-02` | Login/refresh/logout y credenciales inválidas/expiradas; roles y ownership se respetan. |
| `SMK-03` | CRUD/búsqueda de libro; mutación no autorizada falla; stock no queda negativo. |
| `SMK-04` | Reserva válida consume una unidad; retry no duplica; cancelación/expiración libera; cupo/stock fallido no altera datos. |
| `SMK-05` | Angular cubre login, usuarios, membresías, catálogo, carrito, reservas admin/usuario y reportes en desktop/móvil. |
| `SMK-06` | PDFs equivalen funcionalmente al legacy y no filtran reservas ajenas. |
| `SMK-07` | Conteos, claves y totales de cada migración coinciden al 100 % salvo excepción aprobada y documentada. |
| `SMK-08` | Aplicación soporta reinicio de servicio/Rabbit y restore de BD dentro de RPO/RTO aprobados. |

## Etapas de despliegue

| ID | Planning | Topología/corte | Rollback principal |
| --- | --- | --- | --- |
| `DEP-00` | `WP-00` | Monolito 8000/MySQL; baseline, secretos rotados y observabilidad inicial. | JAR/config previa y restore ensayado. |
| `DEP-01` | `WP-01` | Reactor/contratos aún sin cambiar tráfico. | Binario compatible; forward-fix de migración. |
| `DEP-02` | `WP-02` | Gateway/Eureka/Compose enrutan al monolito y servicios vacíos. | Ruta directa/estable al monolito. |
| `DEP-03` | `WP-03` | `/books` e inventario a Catalog/PostgreSQL. | Congelar escritor, reconciliar y revertir ruta sólo si MySQL puede retomar sin pérdida. |
| `DEP-04` | `WP-04` | Reservas/membresías a Circ/PostgreSQL; Catalog conserva stock. | Pausar comandos, resolver holds y aplicar replay/reversa ensayada. |
| `DEP-05` | `WP-05` | Auth/usuarios a IAM/PostgreSQL; JWT en todos los servicios. | Compatibilidad de claves, no revertir hashes mejorados ni revocaciones. |
| `DEP-06` | `WP-06` | Angular toma todas las rutas; Thymeleaf queda sólo como fallback temporal. | Revertir assets/ruta UI sin revertir APIs/datos. |
| `DEP-07` | `WP-07` | Report y Rabbit/outbox; Jasper monolítico deja de recibir tráfico. | Reprocesar eventos/proyecciones; fallback PDF temporal autorizado. |
| `DEP-08` | `WP-08` | Ensayo integral, seguridad, rendimiento y DR. | Último release conocido, sin cambios destructivos. |
| `DEP-09` | `WP-09` | Corte 100 %, ventana de observación, apagar monolito/MySQL y retirar recursos tras retención. | Antes de retirar: rutas/imágenes preservadas y plan data-aware; después de retiro: restore/redeploy formal, no toggle instantáneo. |

## Procedimiento de release y rollback

### Release

1. Identificar `WP-*`, `DEP-*`, artefactos, digests, configuración, migración y dueño de decisión.
2. Validar contratos, pruebas, scans, secretos, backup y restore.
3. Aplicar migraciones compatibles y desplegar sin tráfico.
4. Confirmar liveness/readiness, registro Eureka, logs, métricas, trazas y timeouts.
5. Ejecutar `SMK-*`, conciliación y prueba de autorización.
6. Mover una ruta/cohorte; observar contra baseline; completar tráfico y deshabilitar escritor anterior.
7. Conservar rollback hasta aceptación y registrar evidencia cruzada.

### Rollback

Se dispara ante bypass de seguridad, diferencia de datos, reserva/stock inconsistente, readiness fallida, smoke fallido o umbral SLO aprobado excedido.

1. Detener avance, jobs y nuevas escrituras afectadas.
2. Clasificar fallo de ruta, aplicación o propiedad de datos.
3. Para fallo stateless, volver al digest/ruta conocida y repetir smoke.
4. Para fallo stateful, conciliar y replay/compensar antes de habilitar un escritor anterior.
5. Rotar/revocar claves si hubo exposición; preservar logs y auditoría.
6. Verificar invariantes y documentar RTO, disposición de datos, causa y gate de reintento.

## CI/CD objetivo

Pipeline por pull request: formato/lint -> unitarias -> arquitectura/contratos -> PostgreSQL/Rabbit integration -> Angular tests -> SAST/dependency/secret scan -> build. Pipeline de main/tag: imágenes/SBOM/firma -> entorno efímero Compose -> E2E/smoke/security -> registry. Promoción dev -> staging -> producción usa el mismo digest, migración aprobada, environment protection y aprobación PO/operaciones para cortes de datos. Producción usa rolling/blue-green por servicio y route switch en Gateway; no usa `latest`.

## Comandos y estado

Verificados hoy en SAIBM: `./mvnw test`, `./mvnw clean package`, `./mvnw spring-boot:run` y equivalentes Windows `mvnw.cmd`, sujetos a Java/MySQL/Docker de tests. Los futuros `docker compose`, builds del reactor, Flyway, `pnpm` y scripts smoke **no son comandos existentes de SAIBM**; se documentarán como ejecutables recién cuando sus archivos sean implementados y CI los pruebe.

## Cierre operacional

`DEP-09` termina sólo cuando `SMK-01..08` pasan, bases están conciliadas, no hay tráfico/jobs/consultas legacy, backups cumplen retención, dashboards y alertas tienen responsable, y se eliminan despliegue monolítico, rutas Thymeleaf, procedimientos y acceso MySQL. Esto materializa la retirada total exigida por `WP-09`, no una convivencia indefinida.
