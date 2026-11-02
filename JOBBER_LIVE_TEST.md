# Jobber live validation — test account only

Your confirmed test records:
- Job **Salt Application Service** (#1), 7 completed visits dated Oct 26–Nov 1, 2026.
- Draft invoice #1: **6 × €75 = €450**.
- Expected review finding: **quantity shortfall 1, amount €75** (assuming each visit represents one billable application).

## Step A: Verify the data shape in Jobber GraphiQL

Use your existing authorized GraphiQL session (do not expose credentials). First retrieve the encoded job ID:

```graphql
query GetJobsForAudit {
  jobs(first: 10) {
    nodes { id jobNumber title }
  }
}
```

Copy the `id` field for jobNumber `1` into the **GraphiQL variables** for this query:

```graphql
query AuditSnapshotCheck($id: EncodedId!) {
  job(id: $id) {
    id
    jobNumber
    title
    billingType
    completedAndUninvoicedVisitsCount
    completedAndUninvoicedVisitsTotal
    lineItems(first: 10) { nodes { id name quantity unitPrice } }
    visits(first: 20) {
      nodes {
        id title isComplete completedAt
        lineItems(first: 10) { nodes { id name quantity unitPrice } }
        invoice { id invoiceNumber }
      }
      pageInfo { hasNextPage endCursor }
    }
    invoices(first: 10) {
      nodes {
        id invoiceNumber invoiceStatus
        lineItems(first: 10) { nodes { id name quantity unitPrice } }
      }
      pageInfo { hasNextPage endCursor }
    }
  }
}
```

GraphiQL variables (replace JOB_ENCODED_ID with your actual `id`, not Job #1):

```json
{ "id": "JOB_ENCODED_ID" }
```

## Step B: Inspect before running Billing Copilot

Confirm:
1. Exactly 7 `isComplete: true` visits.
2. Each visit has a `Salt Application` `lineItems` node with quantity 1. **If visit line items are empty, do not assume a visit is a billable application**; share the sanitized field shape so we can define a safe count rule.
3. Invoice #1 is draft and has `Salt Application` quantity 6 at unit price 75.
4. The account displays EUR. No conversion is needed; currency should flow from the account, not be hard-coded.
5. The query has no GraphQL `errors`, including field-not-found or permission errors.

Only share a redacted JSON response or screenshots: hide account/client contact details and ALL tokens/secrets. Do not paste Authorization headers.

## Step C: Safety and test status

The regression fixture is *synthetic*, not copied from your real account. It verifies the expected Java calculation once the values have been mapped. A passing GraphiQL query alone is not an end-to-end app test.

**Not yet verified:** the backend running with OAuth-configured real Jobber access, pagination, customer authentication, secrets encryption, full compile/test run, real end-to-end job audit, or a secure live deployment.
