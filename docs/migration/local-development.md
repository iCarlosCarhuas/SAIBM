# Local migration runtime

This runtime starts the implemented platform applications, libro, inventario, RabbitMQ, and migration infrastructure. The obsolete catalog runtime has been retired. Image packaging skips tests because the commands below and CI run tests first; an image build is not test evidence.

## Prerequisites

- Java 21 and the repository Maven wrapper
- Docker Desktop with Compose v2
- An ignored `env.properties` created from `env.properties.example`

Do not commit or print `env.properties`. Keep the generated local-only JWT secret out of every image and log.

Libro writes are fail-closed during this transition: they require a short-lived
signed timestamp/nonce boundary using the existing ignored local secret. Replace
that boundary with service-local JWT authorization when auth-service is available;
the secret value is never documented or logged.

## Verify and start

From the repository root:

```powershell
.\mvnw.cmd test
.\mvnw.cmd -f saibm-platform/pom.xml test
docker compose --env-file env.properties config --quiet
docker compose --env-file env.properties build discovery-server api-gateway libro-service inventario-service membresia-service usuario-service
docker compose --env-file env.properties up -d
docker compose --env-file env.properties ps
```

The optional legacy monolith remains outside Compose. Start it on port 8000 before testing `/legacy/**`:

```powershell
.\mvnw.cmd spring-boot:run
```

Health endpoints:

```powershell
Invoke-RestMethod http://localhost:8761/actuator/health/readiness
Invoke-RestMethod http://localhost:8080/actuator/health/readiness
Invoke-RestMethod http://localhost:8085/actuator/health/readiness
Invoke-RestMethod http://localhost:8086/actuator/health/readiness
docker compose --env-file env.properties exec rabbitmq rabbitmq-diagnostics -q ping
docker compose --env-file env.properties exec iam-db sh -c 'pg_isready -U "$POSTGRES_USER" -d "$POSTGRES_DB"'
```

Gateway is available at `http://localhost:8080`, Discovery at `http://localhost:8761`, libro readiness is internal at `http://localhost:8085`, and RabbitMQ management at `http://localhost:15672`. PostgreSQL and RabbitMQ protocol ports are not published.

## Diagnose and stop

```powershell
docker compose --env-file env.properties ps
docker compose --env-file env.properties logs --tail 100 discovery-server api-gateway libro-service inventario-service membresia-service usuario-service
docker compose --env-file env.properties logs --tail 100 rabbitmq iam-db circulation-db reporting-db
docker compose --env-file env.properties down
```

Use `down` without `-v` so local database volumes survive. If Docker rejects an old API version, clear any `DOCKER_API_VERSION` override and verify the daemon supports the client version. If Gateway is healthy but legacy requests fail, verify the monolith is listening on host port 8000.

Legacy book and stock backfill remains a documented migration gap; it is not a runtime service and must be implemented as an explicit, temporary tool before production cutover.
