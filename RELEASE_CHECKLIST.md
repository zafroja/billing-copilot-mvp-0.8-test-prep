# Release gates and test honesty

**Updated private pilot packaging:** See `DEPLOYMENT_AND_BUILD_REPORT.md`. Docker Compose with HTTPS and a PostgreSQL CI integration-test job were added but NOT executed in this offline container. Existing statuses below remain conservative.

| Gate | Status | Evidence / next action |
|---|---|---|
| Saved real Jobber snapshot quantity arithmetic | **Passed offline** | `standalone-demo` Java test: 7 vs 6 @ €75 |
| Fail-closed on missing approved rate | **Passed offline** | Java demo negative checks |
| Fail-closed on incomplete pagination & GraphQL errors | **Passed offline** | Java demo negative checks |
| React/TypeScript syntax | **Passed syntax transpilation only** | Doesn't replace full `npm run build` |
| AES-256-GCM token encryption | **Passed JDK-only smoke test** | Random IV, round trip, plaintext rejection |
| PostgreSQL Flyway schema | **Static consistency checked** | Needs real DB migration execution; V1/V2 rewritten for clean DB |
| Spring Boot Maven compile/test | **Not executed** | No Maven/dependency registry available in this environment |
| React production build | **Not executed** | npm dependencies unavailable in this environment |
| Jobber OAuth initiated by deployed app | **Not executed** | Needs Jobber app credentials and reachable callback URL |
| Live connected Jobber provider audit | **Not executed** | Authorize same Jobber test account through our app |
| Production customer accounts and tenant isolation | **Not implemented** | Current release is **one private pilot owner only** |
| SaaS billing/subscriptions | **Not implemented** | Stripe account, billing lifecycle and webhook verification needed |
| AI contract extraction | **Not implemented** | Deterministic PDF parser and owner-attested rate only |
| Production privacy, legal, monitoring and security review | **Not completed** | Do not sell multi-customer service yet |

## Required next integration test

- In a network-enabled CI/hosting environment run full backend and frontend builds and DB migrations.
- Connect the app through Jobber OAuth, not via exported GraphiQL credentials.
- Verify account id, job id, all 7 completed visit item records, draft invoice quantity 6, EUR currency configuration and explicitly approved 75/application rule.
- Confirm the live API result flags **one QUANTITY_MISMATCH = €75**.
- Check that no Jobber mutations are issued and no invoices are sent.
- Confirm an invalid password gets 401, an invalid OAuth state is rejected and invalid tenant inputs do not grant access.

This package contains no API credentials or tokens. Do not contact potential customers without owner permission.
