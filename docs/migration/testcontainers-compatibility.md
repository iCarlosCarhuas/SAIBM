# WP-00-A evidence gate

The Spring Boot 3.5.3 / Java 21 boundary remains unchanged while MySQL
evidence becomes reproducible.

## Preflight and receipts

| Item | Value |
|---|---|
| Testcontainers BOM | `2.0.5`; docker-java `3.7.1` |
| Image / Docker | `mysql:8.0.42`; Docker API 1.55, minimum 1.40 |
| Cleanup | Ryuk enabled; no API override or privileged container |

Run Docker version/context/info, named-pipe reachability, Maven-wrapper
availability, the resolved dependency tree, and `mvnw.cmd test`. Successful
evidence starts the real container, executes Spring `@ServiceConnection`
assertions, and performs normal Ryuk cleanup.

Any prerequisite, dependency, startup, binding, assertion, or cleanup failure
is `BLOCKED`: record the exact command, observed state, cause, and remediation
boundary. Failed or skipped receipts never become green.

## Characterization boundary

| Family | Happy path | Rejection/invariant path |
|---|---|---|
| Authentication/registration | valid login | invalid login or duplicate registration |
| Administration | user listing | reservation-protected deletion |
| Catalog | title lookup | reservation-protected deletion |
| Circulation quota/stock | stock decrement | stock/quota rejection |
| Reporting | populated data source | empty data source |

The executable gate rejects incomplete families and 400 or more authored
changed lines. Known legacy authorization findings remain migration debt.

## Rollback boundary

Revert only the dependency, Testcontainers/WP-00-A evidence tests, integration
fixture, and this document. Preserve `TestProfileIsolationContractTests.java`,
route contracts, runtime-secret configuration, and unrelated migration work.
If preflight fails, leave code unchanged and record `BLOCKED`.
