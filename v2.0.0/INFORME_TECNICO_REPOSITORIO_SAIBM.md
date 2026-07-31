# Informe Técnico-Académico de Contraste: Repositorio SAIBM

**Proyecto:** Sistema de Administración e Inventario de Biblioteca Municipal — SAIBM  
**Repositorio:** [`iCarlosCarhuas/SAIBM`](https://github.com/iCarlosCarhuas/SAIBM)  
**Corte auditado:** rama `main`, commit `1c3d1cad5aded0d3e87a875f6339c68373aa1cbc` (*Adding last updates*, 09-07-2025)  
**Fecha de observación:** 30 de julio de 2026  
**Documento contrastado:** `SAIBM_V2_Proyecto_Integrador_avance.pdf` (24 páginas)

---

## Integrantes del Proyecto

| N° | Nombre | Código |
|---|---|---|
| 1 | Luís Alberto Arias Ledesma | i202403378 |
| 2 | Luis Miguel Rosello Silvera | i202416653 |
| 3 | Rosa Amelia Uribe Lopez | I202415764 |
| 4 | Leonardo Fabricio Dorregaray Guevara | i201924621 |
| 5 | Rodrigo Zenteno Gallardo | I202405406 |
| 6 | Juan Adolfo Maguiña Zapata | i202415904 |
| 7 | Flaber Huanca Vasquez | i202123521 |
| 8 | Francisco Brayhan Sánchez García | I201820318 |
| 9 | Johanna Daniela Ñingle Pérez | i202415805 |
| 10 | Carlos Raul Carhuas Tenorio | i202412137 |

---

## Resumen Ejecutivo

Muchachos, miren, esto es lo que pasó. Nos sentamos a revisar el código fuente de SAIBM con lupa, comparando cada línea contra lo que el PDF de 24 páginas nos dice. ¿Y qué encontramos? Encontramos que el sistema actual es un **monolito web** hecho con Spring Boot, Thymeleaf y MySQL — nada de microservicios, nada de Angular, nada de PostgreSQL. Todo eso que el PDF describe como si ya estuviera funcionando es, en realidad, una **propuesta futura** (el TO-BE). Y está bien que sea así — el proyecto está creciendo — pero hay que ser honestos: lo que YA funciona es otra cosa.

¿De acuerdo? Entonces, este informe les va a servir para saber exactamente qué corregir en el PDF, qué diagramas ajustar y qué afirmaciones mover a la sección de "propuesta arquitectónica". Vamos paso a paso.

---

## Recorrido de Lectura

Antes de meternos, les dejo una guía para que cada quien sepa por dónde empezar:

| ¿Quién eres? | ¿Qué leer? | ¿Qué obtienes? |
|---|---|---|
| **Autor del PDF** | Secciones 1, 6 y 8 | Qué cambiar, dónde y con qué texto. |
| **Docente evaluador** | Secciones 2, 3 y 5 | Trazabilidad entre lo que dice el PDF y la evidencia real del código. |
| **Revisor técnico** | Secciones 4, 7 y anexos | Arquitectura actual, riesgos activos y comandos para verificarlo ustedes mismos. |

---

## 1. Hallazgos Prioritarios

Miren, aquí les dejo los hallazgos más importantes. Cada uno tiene su evidencia — no es que estemos inventando nada.

| ID | Hallazgo | ¿Qué nos dice la evidencia? | ¿Qué impacto tiene en el PDF? |
|---|---|---|---|
| H-01 | La base de datos configurada es **MySQL**, no PostgreSQL. | `application.properties:4-8` y `pom.xml:58-61` | PostgreSQL por servicio es TO-BE. |
| H-02 | Es una **única aplicación Spring MVC** con Thymeleaf, no Angular ni microservicios. | `pom.xml:34-49`; carpeta `templates/`; controladores en `controller/` | Angular, gateway y servicios son diseño futuro. |
| H-03 | El flujo transaccional real es la **reserva de libros**; no existen préstamos, devoluciones, ejemplares, inventario ni adquisiciones. | Entidades y controladores revisados | Reducir el AS-IS a lo que realmente está codificado. |
| H-04 | El repositorio es **público**, no privado. | `gh repo view` devolvió `isPrivate: false` el 30-07-2026 | Eliminar toda referencia a "repositorio privado". |
| H-05 | La única prueba versionada **no pasó** en el ambiente de revisión. | `mvnw.cmd test`: 1 prueba, error de inicialización JDBC/JPA | No afirmar pruebas verdes ni cobertura. |
| H-06 | Hay **secretos y credenciales versionados** en el repositorio. | `env.properties`, `application.properties:16-17`, `data.sql:103-112` | Riesgo de seguridad; reportar sin exponer valores. |
| H-07 | La protección de rutas es **insuficiente**; el interceptor no demuestra bloquear ejecución. | `SeguridadInterceptor.java:17-32` | Documentar riesgo actual, no solo futuro. |
| H-08 | reCAPTCHA depende **síncronamente** de Google sin controles resilientes. | `ReCaptchaService.java:15-22` | Riesgo de disponibilidad y latencia. |
| H-09 | Confirmar múltiples reservas puede dejar **persistencia parcial**. | `ReservaController.java:73-107` | No describir reserva múltiple como atómica. |

---

## 2. Arquitectura y Stack AS-IS

Ahora sí, vamos a lo gordo. ¿Qué es SAIBM en la realidad? Miren este diagrama de capas:

```
Navegador del usuario
  └─ Plantillas Thymeleaf + WebJars (Bootstrap, jQuery)
       └─ Controladores Spring MVC
            └─ Servicios e Implementaciones
                 └─ Repositorios Spring Data JPA
                      └─ Una base de datos MySQL configurada: SAIBM
```

La aplicación arranca en `SaibmApplication.java:6-10`. Los controladores devuelven **vistas** (por ejemplo, `biblioteca/listar_libros` en `LibroController.java:80`), lo que significa que la interfaz está **renderizada en el servidor**. No hay una API REST pública identificada.

### Stack tecnológico verificado

| Componente | Tecnología | Evidencia |
|---|---|---|
| Runtime | Java 21 | `pom.xml:29-31` |
| Framework | Spring Boot 3.5.3 | `pom.xml:5-10` |
| Web / UI | Spring MVC + Thymeleaf + Layout Dialect | `pom.xml:38-49` |
| Persistencia | Spring Data JPA / Hibernate + MySQL | `pom.xml:33-36`, `application.properties:4-8` |
| Informes | JasperReports 6.20.0 + iText 2.1.7 | `pom.xml:112-139` |
| Pruebas | `spring-boot-starter-test` (1 prueba de contexto) | `pom.xml:68-71` |

### Lo que NO existe (componentes no evidenciados)

¿Y qué más? Pues que no encontramos nada de esto:

- **Frontend Angular** — ni `package.json`, ni `node_modules`, ni un solo archivo TypeScript
- **Spring Cloud Gateway** — ni dependencias de Spring Cloud
- **Microservicios** — es un proyecto Maven monolítico
- **PostgreSQL** — solo MySQL configurado
- **Kafka / RabbitMQ** — ni eventos ni outbox
- **Spring Security / OAuth2 / JWT** — usa sesión HTTP y hash propio
- **Docker / Kubernetes** — ni Dockerfile ni Compose
- **CI/CD** — ni GitHub Actions, ni Jenkins, ni nada
- **Observabilidad** — ni Actuator, ni Micrometer, ni OpenTelemetry
- **Préstamos, devoluciones, ejemplares, inventario, adquisiciones** — no hay entidades, rutas ni tablas

---

## 3. Inventario Funcional

### 3.1 Rutas MVC verificadas

Miren, estos son los controladores y sus rutas. Cada uno fue verificado en el código fuente:

**Sesión e inicio:**
- `GET /`, `GET /index` → `IndexController.java:9-15`
- `GET/POST /security/login`, `GET/POST /security/register`, `GET /security/logout` → `SecurityController.java:15-82`

**Libros (catálogo básico):**
- Listado, búsqueda, alta, edición y eliminación → `/biblioteca/*` → `LibroController.java:21-111`

**Reservas:**
- Agregar, cancelar desde carrito, confirmar, listar propias, mantenimiento global, eliminar por ID → `/reserva/*` → `ReservaController.java:21-139`

**Usuarios y reportes:**
- CRUD de usuarios → `/usuario/*` → `UsuarioController.java:19-98`
- Generación Jasper → `ReportController.java:23-86`

### 3.2 Capacidades implementadas y sus límites

| Capacidad | ¿Está implementada? | Limitaciones |
|---|---|---|
| Usuarios, roles, perfiles, membresías | Sí, básica | Sesión HTTP, hash SHA-256 sin salt |
| Catálogo de libros | Sí, básica | Solo nombre, descripción, stock, autor, imagen. No hay ISBN, categoría, editorial, idioma, ejemplar ni ubicación |
| Reservas | Sí, parcial | Límites por membresía (2/4/8), descuenta stock, calcula expiración. No hay cola, prioridad, préstamo, devolución, renovación ni sanción |
| Reportes PDF | Implementación estática | Código y plantillas `.jasper` presentes; ejecución no confirmada |

---

## 4. Modelo de Datos Verificable

| Entidad | Tabla | Relación principal | Archivo |
|---|---|---|---|
| `UsuarioEntity` | `usuarios` | N:1 con rol, membresía y perfil | `entity/security/UsuarioEntity.java:8-43` |
| `RolEntity` | `rol` | Asociación perfil-rol | `entity/security/` |
| `PerfilEntity` | `perfil` | Asociación perfil-rol | `entity/security/` |
| `AccesoEntity` | `acceso` | Asociación perfil-rol | `entity/security/` |
| `MembresiaEntity` | `membresia` | Referenciada por usuario | `entity/MembresiaEntity.java:6-18` |
| `LibroEntity` | `libros` | Referenciada por reserva | `entity/LibroEntity.java:7-30` |
| `ReservaEntity` | `reserva` | N:1 con usuario y libro | `entity/ReservaEntity.java:9-35` |

¿Y qué no existe? Pues que no hay tablas ni entidades para `Prestamo`, `Ejemplar`, `Titulo`, `Sancion`, `Inventario`, `Incidencia`, `Orden`, `Recepcion`, `Notificacion` ni `Outbox`. El DDL en `data.sql` no se inicializa automáticamente porque `spring.sql.init.mode=never` está configurado, y además tiene errores de sintaxis (declara claves foráneas antes de las tablas referenciadas).

---

## 5. Matriz de Contraste: PDF vs. Repositorio

Aquí es donde se pone bueno. Vamos a ver qué dice el PDF y qué dice la realidad:

| Afirmación del PDF | Clasificación | Evidencia |
|---|---|---|
| Repositorio privado | **Contradicha** | GitHub lo reportó como público. Aplicar corrección C-01. |
| Monolito MVC | **Confirmada** | Spring MVC, Thymeleaf, JPA, MySQL. |
| Catálogo, usuarios y reservas | **Parcial** | Implementados de forma básica; no hay lectores diferenciados ni ejemplares. |
| Préstamos, devoluciones, renovaciones, sanciones | **No evidenciada** | No hay rutas, entidades ni tablas. |
| Inventario, adquisiciones, notificaciones, analítica | **No evidenciada** | No hay paquetes ni configuración. |
| Reportes | **Parcial** | Código y plantillas Jasper presentes; ejecución no confirmada. |
| Angular, gateway y microservicios | **Propuesta futura** | Sin TypeScript, Spring Cloud, módulos ni gateway. |
| PostgreSQL por servicio y eventos/outbox | **Propuesta futura** | El AS-IS configura MySQL único; no hay broker ni outbox. |
| OAuth2 / OIDC / JWT / MFA | **Propuesta futura** | El AS-IS usa sesión HTTP y hash propio. |
| Docker, Kubernetes, CI/CD y observabilidad | **No evidenciada / propuesta futura** | Sin archivos ni dependencias de plataforma. |

---

## 6. Correcciones para el PDF

Muchachos, aquí les dejo las correcciones concretas que hay que aplicar. No es que el PDF esté mal — es que hay que afinar las afirmaciones para que reflejen lo que realmente existe.

### 6.1 Sustituciones de contenido

- **C-01 — p. 2 y referencias.** Cambiar "El repositorio es privado" por: "Al 30 de julio de 2026, GitHub informa que `iCarlosCarhuas/SAIBM` es público y su rama predeterminada es `main`."
- **C-02 — pp. 2, 5, 7 y 24.** Reemplazar la descripción amplia del producto por: "La implementación auditada cubre usuarios, roles, perfiles, membresías, libros, reservas y artefactos de reportes PDF. Los dominios de circulación, inventario, adquisiciones, notificaciones y analítica se mantienen como alcance objetivo."
- **C-03 — p. 5, tabla AS-IS.** Sustituir "base compartida" por: "Una conexión MySQL local única está configurada; el esquema SQL versionado requiere corrección y no se inicializa automáticamente."
- **C-04 — p. 7, MVP.** Sustituir por: "MVP verificable: autenticación por sesión, registro de usuarios, catálogo básico de libros, reservas con límite por membresía y reportes PDF implementados estáticamente."
- **C-05 — pp. 10-17.** Anteponer a gateway, servicios, PostgreSQL, eventos, outbox, OAuth2, Kubernetes, CI/CD y observabilidad: "Arquitectura objetivo propuesta (TO-BE); no implementada ni configurada en el commit auditado."

### 6.2 Diagramas a ajustar

1. **AS-IS:** navegador → Thymeleaf/WebJars → controladores MVC → servicios → JPA → MySQL. Incluir entidades: Usuario, Rol, Perfil, Membresía, Libro y Reserva.
2. **Dominio:** Marcar como "implementado" solo usuarios/roles/perfiles/membresías, libros, reservas y reportes Jasper. Los demás dominios van como "planificados".
3. **Ciclo del ejemplar y modelo de circulación:** Mover a TO-BE. El código opera `stock` de libro, no ejemplares ni préstamos.
4. **Modelo de datos:** Sustituir la figura con préstamo/ejemplar/sanción por las siete entidades verificadas.

---

## 7. Riesgos Técnicos Activos

### 7.1 Seguridad y autorización

| ID | Riesgo actual | Evidencia | Acción recomendada |
|---|---|---|---|
| H-10 | Secretos y credenciales versionados. | `env.properties`, `application.properties:16-17`, `data.sql:103-112` | Rotar, retirar del repositorio y usar variables de entorno. |
| H-11 | Hash SHA-256 sin salt ni factor de trabajo. | `HashUtil.java:9-15` | Migrar a BCrypt o Argon2. |
| H-12 | Datos semilla incompatibles con login; doble hash potencial. | `data.sql:103-112`, `UsuarioServiceImpl.java:38-74` | Centralizar el hash y cubrir registro/login con pruebas. |
| H-13 | `SeguridadInterceptor` solo redirige; no demuestra detener la ejecución. | `SeguridadInterceptor.java:17-32` | Implementar un interceptor que corte la cadena y probarlo. |
| H-14 | Usuarios autenticados alcanzan CRUD de libros; mantenimiento global de reservas y eliminación por ID sin verificar propietario o rol. | `LibroController.java:84-111`, `ReservaController.java:124-138` | Autorizar cada operación por rol y propietario. |
| H-15 | Reporte de reservas recibe `usuarioId` sin autorización visible. | `ReportController.java:32-62` | Validar sesión y propiedad antes de generar. |

### 7.2 Confiabilidad y consistencia

| ID | Riesgo actual | Evidencia | Acción recomendada |
|---|---|---|---|
| H-16 | reCAPTCHA depende síncronamente de Google. `RestTemplate` creado por llamada, sin timeouts ni manejo robusto. | `ReCaptchaService.java:15-22` | Cliente reutilizable con timeouts y pruebas de falla. |
| H-17 | Confirmar varias reservas no es atómico. Si falla una, las anteriores pueden persistir y el carrito no se limpia. | `ReservaController.java:90-103`, `ReservaServiceImpl.java:37-74` | Envolver en transacción de aplicación; validar antes de persistir. |
| H-18 | Stock sin control de concurrencia visible. | `ReservaServiceImpl.java:58-64` | Bloqueo optimista/pesimista o actualización atómica. |
| H-19 | Esquema, pruebas y entorno no reproducibles. | `application.properties:4-14`, `data.sql`, resultado de `mvnw.cmd test` | Perfil `test` con base aislada y migraciones válidas. |

---

## 8. Plan TO-BE Recomendado

Este es el plan que les recomiendo para la fase futura. Importante: esto es **propuesta**, no evidencia de que ya existe.

1. **Secretos:** Retirar, rotar y externalizar. Usar variables de entorno o un gestor de secretos.
2. **Pruebas:** Crear perfil de prueba aislado y reparar el DDL hasta que `mvnw.cmd test` pase limpio.
3. **Seguridad:** Sustituir SHA-256 por BCrypt/Argon2. Centralizar el ciclo de hash.
4. **Autorización:** Aplicar Spring Security, autorización por acción/recurso, CSRF y validación con DTOs.
5. **Modelado de dominio:** Modelar título, ejemplar, préstamo y devolución antes de proponer microservicios.
6. **Infraestructura:** Añadir pruebas, CI, health checks y métricas básicas antes de gateway, eventos y despliegue distribuido.

---

## Anexo A. Pruebas y Operación Observables

- La única prueba versionada es `contextLoads` en `SaibmApplicationTests.java:6-11`.
- **Ejecución realizada:** `mvnw.cmd test` terminó con error: una prueba ejecutada, sin fallos de aserción, pero con error al cargar `ApplicationContext` — Hibernate no pudo determinar el dialecto por ausencia de metadatos JDBC.
- No se encontraron archivos de Docker, Kubernetes, CI/CD, cobertura, SAST/DAST, Actuator, métricas, trazas, alertas, backup ni rollback.

---

## Anexo B. Comandos Reproducibles

| Comando | Finalidad | Resultado |
|---|---|---|
| `gh auth status` | Verificar acceso GitHub | Acceso verificado |
| `gh repo view iCarlosCarhuas/SAIBM` | Consultar metadatos | Público, rama `main`, Java/HTML |
| `gh api repos/iCarlosCarhuas/SAIBM/commits?sha=main&per_page=10` | Revisar actividad | Último commit: `1c3d1ca` |
| `git clone --depth 1 --branch main ...` | Inspección local | Clonación correcta |
| `git ls-files` + lectura de fuentes | Inventario estático | 69 archivos, proyecto Maven monolítico |
| `mvnw.cmd test` | Verificación ejecutada | Falló: error JDBC/JPA |

---

## Anexo C. Control Editorial

| ID | Sección PDF | Acción pendiente | Evidencia | Estado |
|---|---|---|---|---|
| C-01 | p. 2, p. 24 | Corregir visibilidad pública | `isPrivate: false` | Pendiente |
| C-02 | pp. 2, 5, 7, 24 | Reducir AS-IS al alcance implementado | Controladores y entidades verificados | Pendiente |
| C-03 | p. 5 | Sustituir PostgreSQL por MySQL | `application.properties:4-8` | Pendiente |
| C-04 | p. 7 | Corregir funcionalidades MVP | No hay préstamo, devolución, inventario | Pendiente |
| C-05 | pp. 8, 14 | Mover ciclo de ejemplar a TO-BE | `LibroEntity` solo tiene stock | Pendiente |
| C-06 | pp. 10-17 | Etiquetar gateway, microservicios, CI/CD como TO-BE | Ausencia de dependencias | Pendiente |
| C-07 | pp. 15, 23 | Añadir brechas de autorización, CAPTCHA, atomicidad | H-13 a H-17 | Pendiente |
| C-08 | p. 23 | Reemplazar afirmaciones de pruebas | `mvnw.cmd test` fallido | Pendiente |

---

## Límites del Informe

- No se ejecutó la aplicación contra una base MySQL real ni se realizaron pruebas manuales de interfaz.
- Los reportes Jasper se clasifican como **implementación estática presente**; no se afirma su generación satisfactoria.
- No se exponen valores de secretos o credenciales; solo su presencia y ubicación.
- La ausencia de componentes se refiere al commit auditado y no excluye trabajo no publicado.

---

*Informe generado el 30 de julio de 2026. Repositorio auditado: [`iCarlosCarhuas/SAIBM`](https://github.com/iCarlosCarhuas/SAIBM), commit `1c3d1ca`.*
