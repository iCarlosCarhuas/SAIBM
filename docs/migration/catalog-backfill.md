# Libro backfill runbook

Book metadata now belongs to `libro-service`; this B01 does not execute a backfill.
Backfill remains opt-in for a later block because the 400-line authored budget is
reserved for the independently deployable service and route cutover. Stock and
holds are intentionally excluded and will be owned by `inventario-service`.

The libro service owns PostgreSQL book-metadata writes. The legacy MySQL database is
a read-only source for the future explicit backfill command and is never dual-written.

## Preconditions

1. Take and verify a MySQL backup.
2. Freeze all legacy `libros` and stock writers.
3. Provision a read-only MySQL account and configure `SAIBM_LEGACY_DB_URL`,
   `SAIBM_LEGACY_DB_USER`, and `SAIBM_LEGACY_DB_PASSWORD` only in the ignored
   local environment.
4. Start libro PostgreSQL and let Flyway complete before importing.

## Import and reconciliation

Run a future explicit migration command only with a dedicated opt-in flag. The
reader selects the descriptive fields from `libros`; it performs no legacy mutation.
Legacy numeric IDs remain migration aliases, while the service uses opaque UUIDs.
Stock is excluded from this backfill and belongs to `inventario-service`, which owns
the target schema, holds, movements, and idempotency. B02 does not execute the
legacy stock/hold backfill: the source freeze, checksummed mapping, and reconciliation
receipt remain an explicit gap before the catalog fallback can be deleted.

The run is accepted only when source/target counts, every canonical descriptive
field, and every migration alias reconcile 100%. Any mismatch is a hard cutover
failure. Repeat the import after correcting the source freeze or target; the
explicit UUID/alias mapping must be rerunnable without duplicates.

## Cutover and rollback

After reconciliation, Gateway smoke, and the combined libro+inventario parity gate,
enable the target routes. Only then may `catalog-service` be removed from the reactor
and Compose. Roll back by disabling target routes/consumers and restoring legacy
routing while catalog volumes are retained for diagnosis. Do not reverse-sync
PostgreSQL into MySQL and do not enable dual-write. Circulation remains on its
existing authority until a coordinated migration.

## Secret handling

Never print the ignored environment file, passwords, JDBC credentials, or internal
admin secret. The normal application startup keeps backfill disabled and does not
create the legacy reader datasource.
