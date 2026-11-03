# Billing Copilot — Build verification & safe private pilot deployment

**Prepared:** 2026-11-02  
**Delivery level:** Source and deployment configuration, **NOT a verified production release**.

## Actual tests performed in this environment

| Check | Result |
|---|---|
| Java 17+ standalone billing engine compiled with `javac --release 17` | PASS |
| Real Jobber response: 7 distinct completed visits, 6 invoiced at €75 | PASS |
| 8 numeric/structural assertions and 4 fail-closed scenarios | PASS |
| Private-pilot compose YAML parsed; only HTTPS reverse proxy has public ports | PASS |
| Source access-control / absent frontend secrets checks | PASS |
| TypeScript/TSX syntax transpilation for application `.ts`/`.tsx` files | PASS (syntax only) |
| Local HTTP smoke: GET health 200, POST approved audit 200/€75, unapproved audit 422 | PASS |
| AES-256-GCM token encryption: round trip, randomized ciphertext, tamper refusal, plaintext refusal | PASS |
| Full Maven build + Spring Boot tests | **NOT RUN**: Maven & artifact registry unavailable |
| Full React `npm run build` | **NOT RUN**: npm dependencies unavailable |
| PostgreSQL Flyway migrations + Hibernate schema verification | **NOT RUN**: PostgreSQL unavailable locally; CI job added |
| Docker image builds, proxy launch and TLS certificate | **NOT RUN**: Docker & public host unavailable |
| Live Jobber API OAuth and audit | **NOT RUN**: user credentials and app hosting required |

Missing toolchain and DNS access are real blockers; do not interpret code syntax checks as full dependency builds.

## Engineering changes in this update

- Private-pilot `docker-compose.private-pilot.yml`: PostgreSQL, Spring backend and React frontend do **not** publish ports. Caddy is the sole public HTTPS entry point; its `/api/*` routing stays on a private Docker network.
- `deploy/Caddyfile` enables automatic TLS, routing, standard security headers and no request-access logging (avoid OAuth codes in callback URLs). A real domain must resolve to the server.
- Frontend API defaults to current browser origin for proxied same-origin HTTPS requests; local dev still supports `VITE_API_BASE_URL`.
- `deploy/prepare_env.py` creates a locked-down `.env` using generated database password and AES key, interactive Jobber credentials and bcrypt owner password (never stored as plaintext). Do not put secrets into chat.
- GitHub Actions now includes PostgreSQL service + `DatabaseMigrationIntegrationTest` and checks three other workflows. Runs once source is pushed to a **private** GitHub repository.
- Backend Dockerfile runs tests during the container build rather than silently skipping them.
- PDF upload validates the `%PDF-` header, file size, and safe base filename.
- Fixed rejected billing rule incorrectly marked human-verified.
- Refresh-token concurrency: reload connection under synchronization, and do not disconnect on network errors.
- One-owner pilot only, no Stripe/AI multi-tenant SaaS release.

## Next test gates — must pass before inviting users

1. In network-enabled GitHub Actions run **all jobs green**, especially Maven/Postgres migration test and React build. Fix any compiler, test, JPA or migration failures exposed by CI; none can be claimed passed today.
2. Use a throwaway VPS/hosting account and a DNS name. Do not use a pre-existing database with a different Flyway history; V1/V2 migrations were rewritten in the prior release.
3. `python3 deploy/prepare_env.py` on a private server. Keep `.env` secure (`chmod 600`), backed up in a trusted secret manager, and **out of source control**. Check `.env` is not in the zip. The helper requires Apache `htpasswd`.
4. Set Jobber Developer Center callback URL to `https://YOUR_DOMAIN/api/integrations/jobber/callback`. Do not use localhost for the hosted app.
5. On server, after CI green: `docker compose -f docker-compose.private-pilot.yml config`; then `docker compose -f docker-compose.private-pilot.yml up --build -d`. TLS depends on inbound ports 80/443, domain DNS, and Caddy's certificate service.
6. Visit `https://YOUR_DOMAIN`, sign in with the owner password, connect the Jobber **test account**, re-login after OAuth callback, and approve the owner's test-only per-application rate (€75). Audit encoded Job #1 `SYNTHETIC_NODE_3`. Expect one quantity mismatch, €75.
7. Verify no invoice edits/sends; inspect logs **without sharing secrets**, database backups/restore, health/monitoring, rate limits, consent/privacy. Restrict server access to your own testers; **do not onboard paying customers yet**.

### Explicit remaining security/release work

Basic authentication is adequate only for a *controlled private pilot over HTTPS*, not a multi-user SaaS. It still needs production user accounts/tenant-level authorization, rate limiting, active monitoring, data retention, incident response, backups and restores, review of OAuth token storage/rotation, privacy/legal readiness, Stripe billing, AI extraction consent, and a complete real Jobber end-to-end test. Deployment configuration alone cannot complete those tasks.

**User action later:** an authorized hosting account, real domain/DNS, confidential Jobber credentials supplied **only to the server**, and authorization in browser. No additional GraphiQL query or test-data creation is required.
