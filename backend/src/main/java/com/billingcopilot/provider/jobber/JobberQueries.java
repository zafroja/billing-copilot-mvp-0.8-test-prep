package com.billingcopilot.provider.jobber;
public final class JobberQueries {
 public static final String ACCOUNT="query GetAccount { account { id name } }";
 public static final String JOBS="query GetJobs($first:Int!,$after:String){ jobs(first:$first,after:$after){ nodes { id jobNumber title } pageInfo { hasNextPage endCursor } } }";
 // Only fields confirmed by the successful 2026-05-12 GraphiQL response are used here.
 // Unrelated timesheets and expenses are excluded from billable-quantity inference.
 public static final String JOB_BILLING_SNAPSHOT="""
 query BillingSnapshot($id: EncodedId!) {
   job(id: $id) {
     id jobNumber title billingType
     completedAndUninvoicedVisitsCount completedAndUninvoicedVisitsTotal
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
 """;
 private JobberQueries(){}
}
