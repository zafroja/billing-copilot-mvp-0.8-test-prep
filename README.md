# Billing Copilot — public source development prototype

Java 17 / Spring Boot / PostgreSQL / React. An experimental, read-only billing quality-assurance tool for field-service businesses using Jobber.

## Status

Development only, **not production-ready or commercially verified**. The source includes a standalone Java audit, a Spring pilot backend, a React UI, synthetic test fixtures, and a GitHub Actions build workflow. Full Maven/React dependency builds, database migrations, live OAuth, and production security still require verification.

## Demo scenario (synthetic)
- Seven completed visits with one salt application each
- Draft invoice contains six salt applications
- Explicitly approved per-application rate €75
- Expected human-review finding: **€75 potential underbilling**

Note: Jobber can classify jobs as fixed-price, so an approved unit-based billing rule is required before interpreting quantity differences as monetary losses. The tool never edits or sends invoices.

## Project structure
- `backend/` — Spring Boot API, JPA, Flyway, Jobber integration adapter
- `frontend/` — React + TypeScript UI
- `standalone-demo/` — dependency-free Java audit and synthetic fixture
- `.github/workflows/build.yml` — Maven, React and Docker Compose checks
- `deploy/` — example deployment configuration

## Local builds
Run `mvn -B verify` from `backend/` and `npm install && npm run build` from `frontend/` using Java 17 and Node 22. Requires external dependency download. The `standalone-demo` Java class can be compiled with just JDK 17.

## SECURITY — PUBLIC REPOSITORY
Never commit `.env`, client secrets, passwords, OAuth tokens, real Jobber IDs, customer information, or unredacted API data. Public source code is not a production deployment.
