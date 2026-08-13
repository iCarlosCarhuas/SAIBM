# Plan ejecutable de migración completa de SAIBM

## Objetivo y reglas de evidencia

Migrar **todas** las capacidades de SAIBM desde el monolito Spring MVC/Thymeleaf/MySQL hacia microservicios Spring Boot, Angular y PostgreSQL. La transición usará Strangler Fig para reducir riesgo, pero termina con el monolito, Thymeleaf y MySQL retirados. “Verificado” significa inspeccionado en archivos; “propuesto” significa trabajo futuro.

## Inventario AS-IS de SAIBM

| ID | Capacidad/activo | Estado verificado y evidencia | Destino |
| --- | --- | --- | --- |
| `AS-01` | Aplicación | Un módulo Maven, Java 21, Spring Boot 3.5.3, Web/JPA/Thymeleaf; `pom.xml`. | Reactor `saibm-platform`. |
| `AS-02` | UI | Thymeleaf, Bootstrap/AdminLTE WebJars, puerto 8000; `templates/`, `application.properties`. | `SVC-WEB`. |
| `AS-03` | Identidad | Login, registro, logout, usuarios, roles, perfiles y accesos; `SecurityController`, `UsuarioController`, entidades `security/`. | `SVC-IAM`. |
| `AS-04` | Seguridad | `HttpSession`, `SeguridadInterceptor`, SHA-256; no Spring Security/JWT. | Spring Security + JWT seguro. |
| `AS-05` | Membresías | Tabla/entidad/servicio de membresía; límites 8/4/2 codificados. | Políticas en `SVC-CIRC`; asignación del plan en IAM. |
| `AS-06` | Catálogo | CRUD/búsqueda de libros, metadatos, imagen y stock. | `SVC-CATALOG`. |
| `AS-07` | Circulación | Carrito en sesión, confirmación, consulta y eliminación de reservas; vigencia dos meses. | `SVC-CIRC`; carrito cliente/servidor explícito. |
| `AS-08` | Consistencia | Confirmación transaccional decrementa stock y guarda reserva. | Reserva de inventario idempotente y compensable. |
| `AS-09` | Datos | MySQL: `perfil`, `rol`, `membresia`, `libros`, `usuarios`, `acceso`, `reserva`; procedimientos de baja cruzada. | Cuatro PostgreSQL sin FKs ni escrituras cruzadas. |
| `AS-10` | Reporting | Jasper PDF de libros y reservas por usuario. | `SVC-REPORT`. |
| `AS-11` | Integración | Google reCAPTCHA en registro/login. | Adaptador IAM, secreto externo; continuidad sujeta al PO. |
| `AS-12` | Operación | Sin Dockerfile, Compose, OpenAPI, Actuator ni Flyway/Liquibase. | Artefactos creados por work packages, nunca asumidos existentes. |

## Referencia real Marketoditito

### Backend inspeccionado

El repositorio `marketoditito-microservicios` es un reactor Maven Spring Boot 3.2.5/Java 17 con módulos `eureka-server`, `api-gateway`, `auth-service`, `catalog-service`, `finance-service` y `reporting-worker` (`pom.xml`). Cada módulo tiene `pom.xml`, aplicación, configuración y Dockerfile. `docker-compose.yml` levanta Eureka, Gateway, tres PostgreSQL, RabbitMQ y servicios; usa perfiles `application-docker.yml`.

| Patrón verificado | Evidencia | Uso en SAIBM |
| --- | --- | --- |
| Reactor multimódulo | `pom.xml` raíz y POMs hijos. | **Reutilizar conceptualmente:** un repositorio backend con módulos desplegables. |
| Eureka + Gateway con `lb://` | `eureka-server/...`, `api-gateway/application.yml`. | **Adoptar adaptando:** rutas SAIBM, CORS por ambiente y seguridad endurecida. |
| PostgreSQL por servicio | `docker-compose.yml`, `application*.yml`. | **Adoptar:** una BD y credenciales por servicio. |
| Dockerfiles multi-stage y Compose | Dockerfiles de seis módulos y Compose. | **Adaptar:** Java 21, usuario no root, tests en CI, health checks y puertos internos. |
| JWT/Bcrypt | `auth-service/SecurityConfig.java`, `JwtService.java`; filtro Gateway/finance. | **Concepto sí; implementación no:** usar Spring Security/Jose, claims estándar y claves rotables. |
| RabbitMQ y reporting worker | finance publisher, `reporting-worker` listener, Compose. | **Adaptar:** eventos de reserva/catálogo para reporting con outbox. |
| Arquitectura hexagonal parcial | `finance-service/domain`, `application`, `infrastructure`. | **Reutilizar donde aporte:** circulación tiene reglas/orquestación complejas; evitar ceremonia en CRUD simple. |

### Frontend inspeccionado

`marketoditito-web` es Angular 21 standalone (`package.json`, `app.config.ts`), con rutas lazy y roles (`app.routes.ts`), guard (`core/guards/auth.guard.ts`), interceptor bearer, servicios HTTP por capacidad, modelos centrales, páginas y componentes compartidos. Usa `environment.apiBaseUrl` y formularios reactivos.

Se reutiliza la estructura `core/services`, `core/guards`, `core/interceptors`, `shared/components`, `pages`, environments y rutas lazy. No se copian páginas financieras, nombres de roles o modelos. Tampoco se copia el token en `localStorage` (`auth.service.ts`): SAIBM preferirá cookie `HttpOnly`, `Secure`, `SameSite` mediante BFF/Gateway; si el PO exige bearer en memoria, requiere threat model y refresh seguro.

### Elementos que NO se copian

- Secretos y contraseñas por defecto de YAML/Compose; JWT compartido `marketoditito-cloud-secret`.
- `ddl-auto: update`; SAIBM usará Flyway y `validate`.
- `SecurityConfig` con `anyRequest().permitAll()` y CSRF deshabilitado globalmente.
- JWT artesanal y autorización sólo en Gateway; cada servicio debe autenticar y autorizar.
- Puertos PostgreSQL publicados fuera del host local; imágenes/Compose sin health checks de aplicaciones.
- Dockerfiles que copian todo el repo y empaquetan con `-DskipTests` como garantía de calidad.
- Entidades JPA devueltas directamente por controladores, rutas financieras, SUNAT, stands/comerciantes y roles `GERENTE`.
- Config Server: **no aparece** en POMs ni configuración de la referencia, por lo que no se adopta en esta migración.

## Arquitectura TO-BE

```text
Browser -> SVC-WEB -> SVC-GATEWAY -> SVC-IAM ----- DB-IAM
                                  -> SVC-CATALOG - DB-CATALOG
                                  -> SVC-CIRC ---- DB-CIRC
                                  -> SVC-REPORT -- DB-REPORT
                         SVC-* <-> SVC-DISCOVERY
SVC-CIRC -> inventory API -> SVC-CATALOG
SVC-CIRC/SVC-CATALOG -> transactional outbox -> RabbitMQ -> SVC-REPORT
```

### Mapa completo de módulos target

| ID | Módulo/repositorio target | Responsabilidad completa | Datos propios |
| --- | --- | --- | --- |
| `SVC-DISCOVERY` | `saibm-platform/discovery-server` | Eureka registry. | Ninguno. |
| `SVC-GATEWAY` | `saibm-platform/api-gateway` | Entrada `/api/**`, TLS/proxy, rutas, CORS, rate limits, correlación y BFF de cookies si se confirma. | Sesión/refresh revocable sólo si opera como BFF. |
| `SVC-IAM` | `saibm-platform/iam-service` | Login, refresh/logout, registro, reCAPTCHA, usuarios, credenciales, roles, perfiles, accesos y asignación de plan. | `users`, `credentials`, `roles`, `profiles`, `permissions`, `user_membership`. |
| `SVC-CATALOG` | `saibm-platform/catalog-service` | Libros, búsqueda, metadatos, imagen, stock disponible, consumo/liberación idempotente. | `books`, `inventory`, `inventory_holds`, `outbox`. |
| `SVC-CIRC` | `saibm-platform/circulation-service` | Planes y límites, carrito persistente opcional, reservas, expiración, cancelación y coordinación de inventario. | `membership_plans`, `carts`, `cart_items`, `reservations`, `idempotency`, `outbox`. |
| `SVC-REPORT` | `saibm-platform/reporting-service` | Proyecciones autorizadas, auditoría de eventos y PDF de catálogo/reservas. | `report_projection`, `audit_event`, `processed_event`. |
| `SVC-WEB` | Repositorio `saibm-web` | Angular completo: login/registro/perfil, usuarios/roles/membresías, catálogo CRUD/búsqueda, carrito, mis reservas, administración de reservas, reportes y errores. | Ninguno transaccional. |

`SVC-REPORT` tendrá base propia porque la meta exige DB por servicio; no será fuente de verdad. Sus proyecciones se reconstruyen desde eventos/APIs. RabbitMQ es infraestructura, no un servicio de dominio.

## Contratos y consistencia

| ID | Contrato v1 mínimo | Propietario/consumidor |
| --- | --- | --- |
| `API-IAM-01` | `/api/v1/auth/login|refresh|logout|me|register`; `/api/v1/users`; `/api/v1/roles`; `/api/v1/memberships/assignments`. | IAM; Web/Gateway/servicios. |
| `API-CAT-01` | `/api/v1/books`, búsqueda/paginación; `/api/v1/books/{id}`; `/api/v1/inventory/{bookId}/hold|commit|release`. | Catalog; Web/Circ. |
| `API-CIRC-01` | `/api/v1/membership-plans`; `/api/v1/carts`; `/api/v1/reservations`; `/api/v1/me/reservations`. | Circ; Web/Report. |
| `API-REP-01` | `/api/v1/reports/books.pdf`; `/api/v1/reports/users/{id}/reservations.pdf`; `/api/v1/audit-events`. | Report; Web/admin. |
| `EVT-01` | `BookChanged`, `ReservationCreated|Cancelled|Expired`, con `eventId`, versión, fecha y IDs; sin PII innecesaria. | Catalog/Circ -> Rabbit -> Report. |

OpenAPI es obligatorio y versionado; DTOs no exponen entidades. Errores usan Problem Details. POST de confirmación exige `Idempotency-Key`. `SVC-CIRC` valida cupo, crea intención y solicita hold; confirma reserva/hold o compensa. No hay transacción distribuida. Eliminaciones se convierten en políticas explícitas: impedir baja con reservas activas o desactivar lógicamente; decisión final del PO.

### Seguridad

- Spring Security en Gateway y **cada servicio**; deny-by-default, roles `ADMIN`/`USER` y autorización por propietario para reservas/reportes.
- Access JWT corto con `iss`, `aud`, `sub`, `iat`, `exp`, `jti`; algoritmo permitido fijo y claves rotables. Refresh revocable/rotativo.
- Passwords nuevos con Argon2id o BCrypt; rehash al login desde SHA-256 tras verificar credenciales legacy.
- Cookie HttpOnly/Secure/SameSite preferida; protección CSRF si hay cookies, CORS allowlist, CSP y headers seguros.
- UUID para nuevos IDs externos; mapa controlado desde IDs legacy durante migración.
- Secretos fuera de Git/imágenes/logs; rotar DB y reCAPTCHA ya expuestos.

## Work packages y sprints

| ID | Sprints | Trabajo y dependencias | Criterio de aceptación |
| --- | --- | --- | --- |
| `WP-00` | 1-2 | Caracterizar todos los procesos `N-PROC-01..06`; inventariar rutas/datos; métricas; backup/restore; rotar secretos. | Pruebas de comportamiento, RPO/RTO y supuestos aprobados por PO. |
| `WP-01` | 3 | Crear reactor/módulos, BOM compatible con Java 21/Spring Cloud, OpenAPI, Actuator, Flyway, errores/logs base. Depende `WP-00`. | Módulos empaquetables, límites y contratos en CI; sin lógica migrada aún. |
| `WP-02` | 4 | Eureka, Gateway, perfiles Docker, Compose base y rutas al monolito/servicios. | Discovery y rutas saludables; rollback al monolito probado. |
| `WP-03` | 5-6 | `SVC-CATALOG`, `DB-CATALOG`, API/inventario, backfill de `libros`. | Contratos, concurrencia stock, conciliación 100 %, único escritor y rollback ensayado. |
| `WP-04` | 7-9 | `SVC-CIRC`, planes, carrito, reservas, idempotencia, compensación; reemplazar procedimientos cruzados. Depende `WP-03`. | Invariantes de cupo/stock bajo concurrencia/fallas; `reserva` migrada y conciliada. |
| `WP-05` | 10-11 | `SVC-IAM`, Spring Security/JWT, rehash, usuarios/roles/perfiles/asignaciones y reCAPTCHA. Depende contratos de Circ. | Matriz de permisos y ownership; migración sin bloqueo de cuentas; rotación/revocación probada. |
| `WP-06` | 12-13 | `SVC-WEB` Angular con todas las pantallas/roles y paridad route-by-route. Depende APIs estables. | E2E responsive/accesible; ninguna capacidad depende de Thymeleaf. |
| `WP-07` | 14 | `SVC-REPORT`, Jasper adaptado, Rabbit/outbox, auditoría y `DB-REPORT`. | PDFs equivalentes, eventos idempotentes/reprocesables, sin acceso a DB ajena. |
| `WP-08` | 15 | Performance, seguridad, resiliencia, observabilidad, migración final, DR y capacitación. | SLO basados en baseline, pentest sin críticos, restore y runbooks aprobados. |
| `WP-09` | 16 | Corte final, apagar escritores/rutas legacy, observar, archivar y retirar monolito/MySQL/Thymeleaf. | Tráfico y jobs legacy cero; datos conciliados; rollback window cumplida; recursos retirados. |

## Estrategia de pruebas y datos

- Unitarias para políticas; arquitectura para límites; repositorios con PostgreSQL real/Testcontainers.
- Contrato provider/consumer para APIs/eventos; integración Gateway/Eureka/Rabbit/DB; E2E Angular.
- Concurrencia para stock/cupos, retries e idempotencia; seguridad para JWT, roles, IDOR, CSRF/CORS y secretos.
- Rendimiento sobre búsquedas, confirmación y PDFs; caos controlado para timeouts, Rabbit y dependencias.
- Migración por servicio: esquema Flyway, snapshot MySQL, transformación, backfill, conteos/hash/muestras, shadow reads, cambio de escritor, observación y contract cleanup.
- No dual-write sin outbox/CDC probado. Cada tabla tiene un escritor y un archivo de mapeo legacy->UUID. Backups no se eliminan al retirar MySQL hasta vencer retención aprobada.

## Trazabilidad con `note.md` y despliegue

| Necesidad | Problemas | Work packages | Deployment |
| --- | --- | --- | --- |
| Procesos completos | `N-PROC-01..06`, `N-PROB-01` | `WP-00..09` | `DEP-00..09` |
| Separación de dominio/datos | `N-PROB-01..04`, `N-PROB-07` | `WP-01`, `WP-03..05`, `WP-07`, `WP-09` | `DEP-01`, `DEP-03..05`, `DEP-07`, `DEP-09` |
| Seguridad | `N-PROB-05..06` | `WP-00`, `WP-05`, `WP-06`, `WP-08` | `DEP-00`, `DEP-05`, `DEP-06`, `DEP-08` |
| Angular completo | `N-PROB-01`, `N-PROB-08` | `WP-06`, `WP-09` | `DEP-06`, `DEP-09` |
| Reporting desacoplado | `N-PROB-09` | `WP-07`, `WP-09` | `DEP-07`, `DEP-09` |
| Operación y retiro total | `N-PROB-08` | `WP-02`, `WP-08`, `WP-09` | `DEP-02`, `DEP-08`, `DEP-09` |

## Definition of Done final

- [ ] Todas las capacidades `AS-03..11` operan en módulos target y Angular.
- [ ] Cada servicio tiene API/eventos, PostgreSQL, Flyway, pruebas, salud, métricas, logs, trazas y rollback.
- [ ] Seguridad, cupos, stock, expiración, bajas y PDFs cumplen caracterización y aceptación del PO.
- [ ] No hay escrituras, lecturas, tráfico, jobs ni reportes que dependan del monolito o MySQL.
- [ ] Monolito, Thymeleaf, procedimientos MySQL y secretos legacy están retirados; backups cumplen retención.
- [ ] `deployment.md` demuestra corte, smoke, conciliación y retirada para `WP-09`.
