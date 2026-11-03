# Jobber API integration test

This public repository contains **synthetic** data only.

For a Jobber developer test account, configure the backend with credentials stored **only** in local environment variables (never Git). Authorize in browser, then verify completed visit quantities against draft invoice line quantities. For the synthetic fixture, the expected discrepancy is seven completed applications minus six invoiced = one unit at an owner-approved rate of €75, hence a **€75 potential issue**.

Do not assume `completedAndUninvoicedVisitsCount` catches quantity mismatches.

The live account flow has not been verified end-to-end from this source.
