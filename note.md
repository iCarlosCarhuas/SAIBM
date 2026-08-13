# Propuesta de modernización integral de SAIBM

Este documento responde el contexto empresarial y académico del **Sistema Administrativo Integral de Membresía para Bibliotecas (SAIBM)**. Los hechos técnicos se sustentan en el repositorio actual; toda afirmación organizacional o económica sin evidencia se marca como **Propuesta** o **Supuesto por validar con el Product Owner (PO)**.

## 1. Definición de la empresa y del sistema

### Identidad y giro

- **Sistema:** SAIBM, descrito en `pom.xml` como “Sistema Administrativo Integral de Membresia para Bibliotecas”.
- **Empresa u organización:** **Supuesto por validar con el PO:** biblioteca o red de bibliotecas que administra usuarios, membresías, catálogo y reservas. El repositorio no acredita una razón social, sede, tamaño ni pertenencia pública o privada.
- **Giro:** gestión de servicios bibliotecarios y acceso de miembros a material bibliográfico.
- **Servicios evidenciados:** registro e inicio de sesión; administración de usuarios; consulta, búsqueda y mantenimiento de libros; carrito y confirmación de reservas; consulta y administración de reservas; aplicación de límites por membresía; generación de reportes PDF de libros y reservas.
- **Usuarios evidenciados:** `ROLE_ADMIN` y `ROLE_USER`, definidos en `src/main/resources/data.sql` y usados por controladores y plantillas.
- **Clientes:** lectores o miembros y personal administrador. **Supuesto por validar con el PO:** bibliotecarios, responsables de atención y dirección utilizan o consumen información del sistema.

### Estructura organizacional propuesta

El código no prueba un organigrama. Para gobernar el proyecto se propone validar esta estructura:

| Área propuesta | Responsabilidad relacionada con SAIBM |
| --- | --- |
| Dirección o Gerencia | Prioridades, presupuesto, indicadores y aceptación final. |
| Atención bibliotecaria | Alta y soporte de miembros, reservas y atención de incidencias. |
| Gestión de catálogo | Altas, cambios, bajas, disponibilidad y calidad de datos de libros. |
| Administración | Usuarios, roles, membresías, reportes y políticas operativas. |
| Tecnología | Desarrollo, seguridad, datos, despliegues, monitoreo y continuidad. |

### Procesos principales

| ID | Proceso evidenciado | Evidencia |
| --- | --- | --- |
| `N-PROC-01` | Autenticación, registro, cierre de sesión y autorización básica. | `SecurityController.java`, `SeguridadInterceptor.java`, plantillas `security/`. |
| `N-PROC-02` | Administración de usuarios, roles, perfiles y membresías. | `UsuarioController.java`, servicios/repositorios y tablas `usuarios`, `rol`, `perfil`, `membresia`, `acceso`. |
| `N-PROC-03` | Consulta, búsqueda, alta, edición y eliminación de libros. | `LibroController.java`, `LibroServiceImpl.java`, tabla `libros`. |
| `N-PROC-04` | Carrito, confirmación, consulta y eliminación de reservas. | `ReservaController.java`, `ReservaServiceImpl.java`, tabla `reserva`. |
| `N-PROC-05` | Control de cupo por membresía y stock. | `ReservaServiceImpl.confirmarReserva`: límites 8/4/2, decremento de stock y vigencia de dos meses. |
| `N-PROC-06` | Reportes PDF de catálogo y reservas por usuario. | `ReportController.java`, `jasperReports/reportlibros.jasper`, `reportjs.jasper`. |

### Misión, visión y objetivos

- **Misión propuesta:** facilitar una gestión bibliotecaria segura y trazable que permita a los miembros consultar y reservar libros y al personal administrar usuarios, catálogo y reportes.
- **Visión propuesta:** operar SAIBM como una plataforma modular, observable y escalable, capaz de evolucionar por servicio sin interrumpir toda la biblioteca.
- **Objetivos:** preservar las reglas de membresía y stock; reducir riesgo de cambios; separar responsabilidades y datos; mejorar seguridad; ofrecer una experiencia Angular completa; automatizar despliegue, pruebas, reportes y recuperación.

## 2. Problemas y causa raíz

No existe evidencia de tiempos de atención, pérdidas monetarias, uso de papel/Excel ni satisfacción de clientes. Esos datos deben levantarse con el PO. Sí se prueban estos problemas técnicos y riesgos operativos:

| ID | Problema evidenciado | Impacto |
| --- | --- | --- |
| `N-PROB-01` | Backend, UI Thymeleaf, seguridad, datos y reportes se empaquetan como una sola aplicación Maven. | Un cambio obliga a probar y desplegar el conjunto; una falla puede afectar todas las capacidades. |
| `N-PROB-02` | Las entidades y procedimientos unen usuarios, libros y reservas en una única base MySQL. | Impide propiedad independiente de datos y despliegue autónomo. |
| `N-PROB-03` | La reserva decrementa stock y crea la reserva en una transacción local. | La separación ingenua rompería atomicidad y podría producir sobreventa o reservas inconsistentes. |
| `N-PROB-04` | Los límites de membresía 8/4/2 están codificados en lógica de aplicación. | Cambiar políticas requiere código y existe riesgo de duplicación/inconsistencia. |
| `N-PROB-05` | Autenticación con `HttpSession`, interceptor propio y SHA-256 sin sal para contraseñas. | Controles frágiles y hash no adaptativo; no es una base adecuada para APIs distribuidas. |
| `N-PROB-06` | Credenciales de BD en `env.properties` y secreto reCAPTCHA en `application.properties`. | Exposición de secretos; deben rotarse y externalizarse antes de desplegar. |
| `N-PROB-07` | Esquema en `data.sql`, procedimientos MySQL y ausencia de Flyway/Liquibase. | Cambios de esquema y migraciones no tienen historial operacional versionado. |
| `N-PROB-08` | No hay Actuator, Docker/Compose, OpenAPI ni frontend Angular en SAIBM. | Falta una base repetible de contratos, salud, empaquetado y despliegue. |
| `N-PROB-09` | JasperReports consume entidades y DTOs del monolito. | Reporting queda acoplado a persistencia y despliegue transaccional. |

### Cuellos de botella y tareas manuales por validar

- **Inferencia técnica:** el despliegue es un cuello de botella porque existe un único artefacto y una única configuración.
- **Inferencia técnica:** altas/bajas con procedimientos que atraviesan dominios dificultan mantenimiento y recuperación selectiva.
- **Supuesto por validar con el PO:** carga manual de libros, membresías o usuarios, conciliación de stock y distribución manual de reportes.
- **Supuesto por validar con el PO:** indisponibilidad durante releases, errores de stock observados y demoras de atención.
- **Supuesto por validar con el PO:** costo por hora de caída, reproceso y soporte. Sin esos datos no corresponde afirmar pérdidas económicas.

### Causa raíz

La causa raíz técnica es una arquitectura monolítica con límites de dominio no explícitos: presentación, autorización, reglas, persistencia y reporting comparten proceso, modelo y base. La causa organizacional no está evidenciada; debe validarse si provino de alcance académico, crecimiento incremental, restricciones de equipo o falta de prácticas DevOps.

## 3. Antes y después

| Dimensión | Antes, verificado | Después, objetivo |
| --- | --- | --- |
| Entrega | Un JAR Spring Boot/Thymeleaf en puerto 8000. | Angular y servicios independientes detrás de API Gateway. |
| Dominio | Usuarios, membresías, libros, reservas y reportes acoplados. | `SVC-IAM`, `SVC-CATALOG`, `SVC-CIRC`, `SVC-REPORT` con contratos explícitos. |
| Datos | Una base MySQL y FKs/procedimientos cruzados. | PostgreSQL separado por servicio y un único escritor por dato. |
| Seguridad | Sesión, interceptor propio, SHA-256 y secretos versionados. | Spring Security, hash adaptativo, JWT validado por cada servicio, secretos externos y autorización por rol/propiedad. |
| Frontend | Plantillas Thymeleaf y WebJars. | Angular standalone, rutas lazy, guards, servicios HTTP y diseño responsivo. |
| Reporting | Jasper dentro del monolito. | Servicio de reporting autorizado, alimentado por contratos/eventos y sin leer bases ajenas. |
| Operación | Sin contenedores ni health checks verificados. | Dockerfiles, Compose, health/readiness, logs correlacionados, métricas, CI/CD y rollback probado. |
| Resultado final | Monolito como sistema completo. | Monolito retirado totalmente después de migrar tráfico, datos y reportes. |

### Tecnologías objetivo

| Capa | Decisión |
| --- | --- |
| Backend | Java 21, Spring Boot, Spring Web, Validation, Data JPA, Security, Actuator, Maven reactor. |
| Plataforma | Spring Cloud Gateway y Eureka, adoptados de la referencia real Marketoditito; configuración por ambiente en cada módulo, sin Config Server porque la referencia no contiene uno. |
| Contratos | REST/JSON, OpenAPI, errores normalizados e idempotency keys para comandos sensibles. |
| Datos | PostgreSQL 16 por servicio y Flyway; migración controlada desde MySQL. |
| Mensajería | RabbitMQ sólo para eventos de reporting/auditoría con outbox; no para reemplazar la coordinación reserva-stock. |
| Seguridad | JWT de corta duración, Spring Security, algoritmo/issuer/audience explícitos, rotación de claves y BCrypt o Argon2id. |
| Frontend | Angular, TypeScript, RxJS, formularios reactivos y CSS/Tailwind sujeto al diseño; BFF/cookie HttpOnly preferido frente a `localStorage`. |
| Reporting | JasperReports inicialmente reutilizado dentro de `SVC-REPORT`, desacoplado de entidades y bases ajenas. |
| Operación | Docker, Docker Compose, CI/CD, logs JSON, métricas, trazas y alertas. |

## 4. Gestión Scrum

### Equipo y gobierno

| Rol | Responsabilidad |
| --- | --- |
| Product Owner | Validar supuestos, políticas, prioridades y aceptación de negocio. |
| Scrum Master | Facilitar Scrum, remover impedimentos y proteger objetivos del sprint. |
| Arquitecto/Backend | Contratos, servicios, seguridad, consistencia y migración. |
| Frontend | Angular, accesibilidad, UX y contratos cliente. |
| QA | Caracterización, contrato, integración, seguridad, rendimiento y E2E. |
| Datos/DevOps | PostgreSQL, Flyway, backfill, observabilidad, CI/CD y recuperación. |

Se mantendrán Product Backlog, Sprint Planning, Daily Scrum, refinamiento, Sprint Review y retrospectiva. Cada incremento debe incluir código, pruebas, documentación, observabilidad y rollback; aunque la transición sea incremental, el objetivo de producto es retirar todo el monolito.

### Cronograma realista estimado

**Estimación:** 32 semanas, 16 sprints de dos semanas, para un equipo estable de 5-6 personas. Debe recalibrarse tras `WP-00` de `planning.md`.

| Sprints | Resultado |
| --- | --- |
| 1-2 | Descubrimiento, métricas base, caracterización, secretos y migraciones versionadas. |
| 3-4 | Reactor target, contratos, seguridad base, Eureka, Gateway y Compose mínimo. |
| 5-6 | `SVC-CATALOG` y su PostgreSQL; migración de libros y stock. |
| 7-9 | `SVC-CIRC`; membresías, reservas, idempotencia y coordinación de inventario. |
| 10-11 | `SVC-IAM`; usuarios, roles, perfiles, hash y transición JWT. |
| 12-13 | `SVC-WEB`; paridad Angular de todas las rutas y roles. |
| 14 | `SVC-REPORT`, eventos/auditoría y PDFs. |
| 15 | Migración final de datos, rendimiento, seguridad, DR y capacitación. |
| 16 | Cutover final, observación y retirada completa del monolito/MySQL/Thymeleaf. |

### Costos estimados

Montos referenciales en soles, sin cotizaciones ni datos salariales de la organización. **Supuesto:** 32 semanas, equipo parcial/mixto y despliegue inicial con Compose. El PO debe validar alcance, tarifas e infraestructura.

| Concepto | Estimación |
| --- | ---: |
| Análisis, arquitectura y gestión | S/ 24,000 |
| Backend, seguridad y contratos | S/ 56,000 |
| Frontend Angular | S/ 28,000 |
| Datos y migración | S/ 20,000 |
| QA, seguridad y rendimiento | S/ 24,000 |
| DevOps, observabilidad y despliegue | S/ 20,000 |
| Capacitación y documentación | S/ 8,000 |
| Contingencia 15 % | S/ 27,000 |
| **Total estimado** | **S/ 207,000** |

Infraestructura recurrente, licencias, soporte y operación no pueden estimarse responsablemente sin SLA, carga, retención y proveedor objetivo.

### Entregables

- Backlog validado y matriz de supuestos del PO.
- Arquitectura AS-IS/TO-BE y ADR de decisiones críticas.
- Reactor backend con Eureka, Gateway, cuatro servicios y contratos OpenAPI.
- Angular completo para autenticación, usuarios, membresías, catálogo, reservas y reportes.
- Cuatro bases PostgreSQL, migraciones Flyway, scripts de backfill y conciliación.
- Dockerfiles, Compose, variables de ejemplo sin secretos, runbooks y rollback.
- Pruebas unitarias, contrato, integración, concurrencia, seguridad, E2E y smoke.
- Dashboards, alertas, trazas, manual técnico, manual de usuario y capacitación.
- Evidencia de corte, retirada total del monolito, MySQL y Thymeleaf.

La ejecución detallada, IDs y criterios están en [`planning.md`](planning.md); el despliegue asociado está en [`deployment.md`](deployment.md).
