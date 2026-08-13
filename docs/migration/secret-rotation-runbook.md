# Secret Rotation Runbook

Removing credentials from Git does not invalidate them. The database and reCAPTCHA owners must rotate the exposed values in their respective environments.

## Required Configuration

The monolith requires these environment properties at startup:

- `SAIBM_DB_URL`: database endpoint and JDBC options; sensitive infrastructure configuration.
- `SAIBM_DB_USER`: database account name.
- `SAIBM_DB_PASSWORD`: database password; secret.
- `SAIBM_RECAPTCHA_SITE_KEY`: public, client-visible site key; configurable but not secret.
- `SAIBM_RECAPTCHA_SECRET`: server-side verification secret.

For local development, copy `env.properties.example` to the ignored `env.properties` file and replace every placeholder. Deployment environments should inject the same names through their secret/configuration manager. Missing required values cause startup configuration to fail rather than using fallback credentials.

## Rotation Steps

1. Inventory every environment using the exposed database account and reCAPTCHA key pair.
2. Create a replacement database credential with only the permissions required by the monolith.
3. Store the replacement database values in each environment's secret manager under the required names.
4. Create or rotate the reCAPTCHA key pair in the provider console, preserving the allowed hostnames for each environment.
5. Store the new site key as public configuration and the new secret only in the server-side secret manager.
6. Redeploy or restart one non-production instance, then production instances according to the environment owner's rollout procedure.
7. Revoke the old database credential and reCAPTCHA secret only after validation succeeds everywhere.
8. Review repository history, CI logs, build artifacts, backups, and shared channels for further exposure; restrict or purge them according to organizational policy.

## Validation

- Confirm startup reports no unresolved required property.
- Confirm the application can perform a representative read and write using the least-privileged database account.
- Confirm the reservation flow accepts a valid reCAPTCHA response and rejects an invalid one.
- Confirm the old database credential can no longer authenticate after revocation.
- Confirm the old reCAPTCHA secret is no longer accepted after revocation.
- Confirm no secret value appears in application logs, CI output, generated artifacts, or browser-delivered content.

## Rollback

If deployment validation fails before old credentials are revoked, restore the previous secret-manager version and restart the affected instance. Keep the source configuration externalized; do not restore literals to tracked files.

If old credentials were already revoked, create another replacement credential or key through the owning service. Do not reactivate or copy an exposed value back into configuration. Record the failed rotation, affected environments, and final revocation status without recording secret values.
