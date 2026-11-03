# 2026-11-02 — Billing Copilot security / pilot upgrade

## Implemented in source

- Corrected inconsistent tenant schema identifiers in the **new, clean-database migration chain** (not safe to overwrite existing databases).
- Added Spring Security single-owner HTTP Basic authentication, bcrypt hash from env, restricted CORS origin, no persistent browser credential storage, and a separate status probe requiring credentials.
- Restricted contract/rule access to pilot owner scope; server binds audit history to fixed pilot tenant instead of trusting arbitrary caller IDs. Stored approved rules required for provider audits.
- Added owner-attested manual rate entry for the exact `Salt Application`, EUR 75/application test scenario; no fabricated PDF required.
- Added read-only persisted audit history and evidence screen; saved contracts reload after full-page OAuth redirect; explicit currency display.
- Added cloud build Dockerfiles, localhost-only compose stack, CI build workflow and startup checks for Jobber/token secrets.

## Verified in this environment

- Java 17 standalone fixture audit PASS; 8 real-data assertions and 4 guardrail cases.
- TypeScript/TSX syntax parsing PASS for all frontend source modules (not full type-check/bundle).
- Java 17 AES-256-GCM encryption roundtrip/nonce/legacy rejection PASS.
- Static migration naming/foreign-key reference checks PASS (not PostgreSQL execution).

## Not verifiable here

- Maven/JUnit full Spring build and database migrations (no Maven or registry access).
- npm/React production build (no npm dependencies or registry access).
- Live app-initiated Jobber connection (requires credentials and HTTPS callback).
- Multi-customer authentication, Stripe subscription fulfillment, complex AI contract extraction, and production security certification.

**No external outreach or Jobber mutation occurred.**
