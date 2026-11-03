# Render deployment — Billing Copilot test account only

The repo includes `render.yaml` and `Dockerfile.render` for a **single-origin private test pilot**: React is bundled into the Spring Boot JAR; one Render HTTPS web service serves the UI and APIs, with one Render PostgreSQL database.

## Before you apply the Blueprint

- Read the costs on Render's review screen. The repository currently declares **Free** for the web service and Postgres. Render's free Postgres expires after **30 days**, has **no backups**, and is **not suitable for real customers**. Web services may spin down when idle, and the Java service may need more than the free tier's 512 MB of memory.
- This pilot uses only fictional Jobber test-account records. **Do not submit customer contracts, personal information or real payment data.**
- Do not put passwords, OAuth tokens, client secrets, or real Jobber JSON/IDs into public GitHub commits or this chat.
- Do not enable Stripe or general customer access yet.

## Create from your Render account (phone works)

1. In [Render Dashboard](https://dashboard.render.com/), select **New → Blueprint** (or **Blueprints → New Blueprint Instance**).
2. Authorize GitHub and select repository `zafroja/billing-copilot-mvp-0.8-test-prep` and branch `main`.
3. Render reads the root `render.yaml`. Review the services and pricing **before** clicking Apply. Both service definitions should show Free; **do not purchase paid resources without agreeing to costs**.
4. When Render prompts for the secrets, enter `JOBBER_CLIENT_ID` and `JOBBER_CLIENT_SECRET` **directly in Render**, copied from the Jobber Developer Center, never into the GitHub repo or ChatGPT. No real access token is needed. Render supplies database credentials automatically.
5. Render automatically creates `PILOT_PASSWORD_RAW` and `TOKEN_ENCRYPTION_KEY_B64`. Copy/store the owner bootstrap password from Render's private Environment settings. **Keep the token encryption key stable** across redeploys (old tokens cannot be decrypted with a replacement key). Username is `owner`.
6. Wait for a successful deploy and locate your own **actual** Render URL under the web service, e.g. `https://<assigned-name>.onrender.com`. Do **not** guess it.
7. Test `https://<actual-url>/api/health` for `{"status":"ok"}`. Open the site and sign in with username `owner` and the generated password. If deployment fails, inspect Render's **Deploys → Logs**, remove any secret values and send only the error text.
8. In your **existing** Jobber Developer Center app settings, change the OAuth callback URL to `https://<actual-url>/api/integrations/jobber/callback` (using the *actual* assigned host). Save.
9. On the Billing Copilot site, select **Connect Jobber**, authorize your **test business account**, enter and confirm the €75/application test rate, then audit Job #1. Expected **potential** discrepancy: 7 completed vs 6 billed = €75, subject to fixed-price billing rules and human approval.

## Technical facts

- API routes except `GET /api/health` and the OAuth callback are owner-protected. Public `/`, `index.html`, and `/assets/*` allow the login screen to render. All data, rule approval, audit history and Jobber connect endpoints remain protected.
- The Docker image runs a multi-stage Node 22 + Maven/Java 17 build and starts Spring Boot on Render's `PORT`.
- The internal Render PostgreSQL URL is converted to a JDBC URL at startup; database username and password come from Render's secret values.
- OAuth callback and frontend origin use Render's `RENDER_EXTERNAL_URL`; the final HTTPS URL must also be registered with Jobber.
- Always use HTTPS. Basic authentication credentials are held in browser memory by the React prototype and must **not** be used as a public multi-user SaaS authentication system.
- This is not a production-ready product; production identity, authorization, rate limiting, backups, monitoring and payments remain separate release gates.

## Rollback and privacy

If in doubt, disconnect the Jobber test app from Billing Copilot and shut down the web service. Free Postgres data is temporary. If secrets were accidentally committed to GitHub, rotate them at the provider immediately; removing the file alone does not erase git history.

## Build evidence

[GitHub Actions](https://github.com/zafroja/billing-copilot-mvp-0.8-test-prep/actions) runs Maven tests with PostgreSQL, React/Vite production build, a standalone Java test, Docker Compose validation, and a combined Render Docker image build. **A CI build does not prove the live Jobber OAuth flow or the deployed website works.**
