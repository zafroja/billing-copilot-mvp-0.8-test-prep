# Billing Copilot — first private pilot

**Latest:** See `DEPLOYMENT_AND_BUILD_REPORT.md` and `NEXT_STEPS.md` for the new HTTPS reverse-proxy deployment and CI PostgreSQL test. The full Spring and React production builds have NOT been verified yet.

**Status (2026-11-02):** Source updated; standalone Java real-snapshot audit tests pass. Full Spring Boot/React dependency builds, PostgreSQL integration and app-initiated Jobber OAuth remain unverified. **Not approved for public/customer deployment.**

## What works without credentials

- Open `billing-copilot-demo-preview.html` for the portable browser preview (pre-recorded data).
- In `standalone-demo/`, run `javac --release 17 -d out src/BillingCopilotDemo.java` then `java -cp out BillingCopilotDemo --test`.
- The real saved Jobber GraphQL snapshot contains seven completed visits, six invoiced salt applications, and €75/app; the offline audit reports one €75 **review finding**.

## Private Spring pilot in a network-enabled development environment

1. Upload/unzip this entire source folder to a secure development machine or **private** repository.
2. Build/test in GitHub Actions (`.github/workflows/build.yml`) or run: `cd backend && mvn test` and `cd frontend && npm install && npm run build`.
3. Use a fresh dedicated PostgreSQL database. The historical V1/V2 migrations were corrected for the pilot; **do not run this rewritten migration chain against a preexisting database without a reviewed migration plan**.
4. Configure secrets through an environment/secret manager, never source code:
   - `DATABASE_PASSWORD`, `PILOT_USERNAME`, `PILOT_PASSWORD_BCRYPT`
   - `TOKEN_ENCRYPTION_KEY_B64` (32 secure random bytes encoded in base64)
   - `JOBBER_CLIENT_ID`, `JOBBER_CLIENT_SECRET`, `JOBBER_REDIRECT_URI`
   - `APP_FRONTEND_ORIGIN`, `PILOT_CURRENCY=EUR`, `JOBBER_API_VERSION=2026-05-12`
   - For direct browser frontend builds, `VITE_API_BASE_URL=https://YOUR-BACKEND-HTTPS-ADDRESS`
5. Generate a password hash with `htpasswd -nB owner` and copy only the bcrypt value after `owner:`; use a strong unique password. For a local machine with Python installed, a secrets manager can also generate a bcrypt value. **Never send plaintext passwords or secrets in chat.**
6. Use HTTPS for frontend/backend for any non-local usage. Set `SERVER_BIND_ADDRESS=0.0.0.0` only behind an authenticated HTTPS reverse proxy, firewall and monitored hosting. The docker compose file binds ports to localhost for safety.
7. Run the backend in `jobber` profile, deploy frontend, sign in, connect Jobber using its official OAuth flow, and re-sign-in after Jobber redirects (the owner password is not kept across full-page navigation).
8. In **Contracts**, manually attest the test rule `Salt Application / 75 / application` with a source note, then under **Integrations**, run Job #1: `SYNTHETIC_NODE_3`. Expected: quantity discrepancy €75, no invoice mutation. This is a **test-account example**, not a claim of legally collectible revenue.

## Boundaries

- **Private single-owner pilot only.** All owner API routes require stateless HTTP Basic authentication with a bcrypt password hash; OAuth callback is the sole public endpoint and checks a short-lived random state.
- **Do not connect customer production data** until security review, full integration tests, backups, data-retention rules, monitoring, webhook disconnect handling and privacy/legal approval are completed.
- Jobber refresh/access tokens are encrypted at rest. The secret key must remain stable across restarts. Lost key requires reconnecting Jobber.
- No invoice creation, editing or sending; no outbound customer contact.
- No production billing/Stripe integration; charge manually for a closed pilot if appropriate and legally ready.
- AI extraction is not connected; PDF extraction is deterministic and manual rate entry is available. Never send customer contracts to third-party AI without informed consent and a privacy assessment.
