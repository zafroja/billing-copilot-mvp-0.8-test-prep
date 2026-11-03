# Billing Copilot — Real Jobber Snapshot Demo

This is a **Java 17+ localhost-only** working demonstration that processes the actual, redacted GraphQL response from Jobber Job #1. It is **not a live Jobber integration or production SaaS**.

## Run without Maven or npm

From `standalone-demo` (requires Java JDK 17 or newer):

```bash
mkdir -p out
javac --release 17 -d out src/BillingCopilotDemo.java
java -cp out BillingCopilotDemo --test
java -cp out BillingCopilotDemo 8787
```

Open **http://127.0.0.1:8787** on the same computer. Tick **Approve €75 per application rule**, then click **Run billing audit**. The server returns HTTP 200 with the result, or 422 if the price rule is not approved. No outgoing Jobber calls or invoice edits occur.

Alternatively, open `../billing-copilot-demo-preview.html` in a browser to see a standalone preview *without Java or a server*. The static preview embeds the result computed by the Java endpoint, and its audit button displays that prerecorded result. It does not execute Java in the browser.

## What passed

- The actual uploaded Jobber JSON parses successfully and yields 7 completed, 6 invoiced, 1 discrepancy of EUR 75.
- Seven distinct completed visits are counted even though all share the same Jobber line-item ID.
- Fixed-price per-application agreement must be explicitly approved.
- Partial pages and GraphQL errors block the audit.
- HTTP endpoint is localhost-bound, returns results, and blocks unapproved audits.
- Browser preview audit flow was interaction-tested using a mocked response from the verified endpoint.

## Important limitations

- The approved rate is **assumed from the owner's test scenario** rather than an actual approved contract document. Confirm real contractual terms before making financial claims.
- The job response does not include account currency; EUR is independently confirmed by the account owner.
- Multiple invoices, more than 10 nested invoice/visit items, partial visits, and cross-invoice associations fail closed rather than silently undercounting.
- The demo's API is specifically for one fixed-price fixture; it cannot audit arbitrary Jobber customers.
- It is not production-safe for remote access, and cannot substitute for a live, secured Jobber OAuth integration.
