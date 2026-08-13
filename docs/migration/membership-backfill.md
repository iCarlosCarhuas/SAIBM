# Membership backfill contract

The `membresia-service` owns plans, versioned limits, assignments, history and aliases. Normal startup keeps `SAIBM_MEMBRESIA_BACKFILL_ENABLED=false`.

The opt-in mapper preserves `membresia.membresia_id` and `usuarios.usuario_id` as migration aliases; it maps legacy plan names through an explicit operator-provided limit map and never reads or writes another service database.

The reconciliation contract compares plan aliases, assignment aliases and counts before any writer switch. A failed reconciliation blocks cutover and leaves the legacy writer untouched.

The operational JDBC reader/writer runner is intentionally deferred to the migration work unit; this slice contains only the mapper, reconciliation contract and disabled configuration. No fake runner or dual write is enabled.

Before activation: freeze legacy membership writers, capture a checksum snapshot, run the opt-in runner, reconcile, retain the rollback snapshot, then switch Gateway traffic. Remove the transitional signed write authorizer only after JWT/BFF service-local authorization is live and verified.
