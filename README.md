# Billing Copilot MVP 0.8 — Jobber live-test preparation

Based on the v0.7 source. Adds a regression fixture and JUnit test for your 7-visits/6-invoiced sample, stronger GraphQL error handling, and excludes timesheets/expenses from billable quantities to avoid false charges. See `JOBBER_LIVE_TEST.md` for the **next exact GraphiQL query**.

**This does not connect to your test account by itself.** No credentials or real customer data were used. Compilation and Maven tests have not been run in this environment (Maven not installed).

Do not distribute this version to paying customers yet: OAuth token encryption and tenant isolation still require production hardening. Currency handling is not wired through all DTOs/UI yet. The numeric amount 75.00 in the Java fixture is intended to represent **€75** in this specific test.
