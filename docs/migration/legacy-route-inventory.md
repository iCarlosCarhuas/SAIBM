# Legacy HTTP route inventory

This WP-00 baseline records the MVC annotations present in the monolith. It is a characterization contract, not approval of the current authorization model. "Current observed access" describes `SeguridadInterceptor`; "target access" is the migration requirement and must be implemented and tested in later work units.

The inventory contains **25 routes**. `/` and `/index` are separate patterns handled by the same Java method. The earlier estimate of 28 was not confirmed by the six controllers currently present.

## Access vocabulary

| Term | Meaning |
|---|---|
| Public | The advice exempts the request from its session redirect. |
| Authenticated | The advice redirects when `usuarioLogueado` is absent, but performs no role or ownership check. |
| Admin | The advice additionally checks `ROLE_ADMIN`; this applies only to paths beginning with `/usuario`. |

`SeguridadInterceptor` calls `sendRedirect` but does not return a decision that prevents the controller method from running. Therefore "Authenticated" and "Admin" below describe the observed redirect gate, not a proven server-side authorization boundary.

## Routes

| HTTP | Pattern | Controller method | Process | Current observed access | Target access |
|---|---|---|---|---|---|
| GET | `/` | `IndexController#index` | N-PROC-01 | Public | Public |
| GET | `/index` | `IndexController#index` | N-PROC-01 | Public | Public |
| GET | `/security/login` | `SecurityController#mostrarLogin` | N-PROC-01 | Public | Public |
| POST | `/security/login` | `SecurityController#procesarLogueo` | N-PROC-01 | Public | Public, with login CSRF and abuse controls |
| GET | `/security/register` | `SecurityController#mostrarRegistro` | N-PROC-01 | Public | Public |
| POST | `/security/register` | `SecurityController#crear` | N-PROC-01 | Public | Public, with registration abuse controls |
| GET | `/security/logout` | `SecurityController#cerrarSesion` | N-PROC-01 | Authenticated; state mutation via GET | Authenticated, state-changing non-GET with CSRF protection |
| GET | `/usuario/listar_usuarios` | `UsuarioController#listarUsuarios` | N-PROC-02 | Admin redirect gate | Admin |
| GET | `/usuario/nuevo` | `UsuarioController#mostrarFormularioRegistro` | N-PROC-02 | Admin redirect gate | Admin |
| GET | `/usuario/editar/{id}` | `UsuarioController#mostrarFormularioEditar` | N-PROC-02 | Admin redirect gate | Admin |
| POST | `/usuario/guardar` | `UsuarioController#guardarUsuario` | N-PROC-02 | Admin redirect gate | Admin, explicit writable-field allowlist |
| POST | `/usuario/eliminar/{id}` | `UsuarioController#eliminarUsuario` | N-PROC-02 | Admin redirect gate | Admin |
| GET | `/biblioteca/listar_libros` | `LibroController#listarLibros` | N-PROC-03 | Authenticated; handler repeats a session check | Authenticated |
| GET | `/biblioteca/nuevo` | `LibroController#nuevoLibro` | N-PROC-03 | Authenticated; admin visibility only in UI | Admin |
| GET | `/biblioteca/editar/{id}` | `LibroController#editarLibro` | N-PROC-03 | Authenticated; admin visibility only in UI | Admin |
| POST | `/biblioteca/guardar` | `LibroController#guardarLibro` | N-PROC-03 | Authenticated; no admin gate | Admin, explicit writable-field allowlist |
| POST | `/biblioteca/eliminar/{id}` | `LibroController#eliminarLibro` | N-PROC-03 | Authenticated; no admin gate | Admin |
| POST | `/reserva/agregar` | `ReservaController#agregarReserva` | N-PROC-04 | Authenticated | Authenticated customer |
| POST | `/reserva/cancelar` | `ReservaController#cancelarReserva` | N-PROC-04 | Authenticated | Authenticated customer |
| POST | `/reserva/confirmar` | `ReservaController#confirmarReservas` | N-PROC-04, N-PROC-05 | Authenticated; handler repeats a session check | Authenticated customer; server-enforced owner, quota and stock rules |
| GET | `/reserva/mis_reservas` | `ReservaController#misReservas` | N-PROC-04 | Authenticated; handler repeats a session check | Authenticated owner |
| GET | `/reserva/mantener_reservas` | `ReservaController#mantenerReservas` | N-PROC-04 | Authenticated; admin visibility only in UI | Admin |
| POST | `/reserva/eliminar/{id}` | `ReservaController#eliminarReserva` | N-PROC-04 | Authenticated; no role or ownership check | Admin, or owner only if cancellation is a supported business operation |
| GET | `/generar/pdf/{usuarioId}` | `ReportController#generarPDF` | N-PROC-06 | Authenticated; caller selects any user ID | Authenticated owner or Admin |
| GET | `/generar/reporteLibros` | `ReportController#generarReporteLibros` | N-PROC-06 | Authenticated; admin visibility only in UI | Admin |

## Security findings retained for migration

- **IDOR:** `GET /generar/pdf/{usuarioId}` accepts an arbitrary user ID without comparing it to the session user. `POST /reserva/eliminar/{id}` likewise deletes by identifier without an ownership or admin check.
- **Missing admin protection:** book maintenance, reservation maintenance, reservation deletion and the catalog report rely on template visibility rather than server-side authorization.
- **GET mutation:** logout invalidates the session through `GET`, allowing cross-site logout and violating safe-method semantics.
- **Redirect is not enforcement:** the global advice sends redirects without explicitly stopping handler execution. The migration must fail closed before business logic runs.
- **Mass assignment:** user and book save handlers bind persistence entities directly from request fields. The target should accept explicit request models and allowlisted fields.
- **Broad public-path matching:** the advice uses `url.contains("login")` and `url.contains("register")`, so future paths containing those substrings could bypass the session redirect unintentionally.

These findings are deliberately not fixed by this work unit. Future services must implement the target access column as independently tested authentication, role and resource-ownership policies.

## Contract and verification

`WebRouteInventoryContractTests` scans every compiled `@Controller` under `edu.pe.cibertec.SAIBM.controller`, reads merged MVC mappings through reflection, normalizes each HTTP method/path/handler signature, and compares deterministic sets. It does not create a Spring application context or connect to MySQL or Docker. Any added, removed or changed route reports missing and unexpected signatures and requires an intentional update to both the test and this document.

Runtime harness is N/A because this work unit characterizes static MVC annotations and opens no runtime boundary.

Rollback: delete only `src/test/java/edu/pe/cibertec/SAIBM/WebRouteInventoryContractTests.java` and `docs/migration/legacy-route-inventory.md`.
