package com.billingcopilot.provider.jobber;
public final class JobberQueries {
 public static final String ACCOUNT="query GetAccount { account { id name } }";
 public static final String JOBS="query GetJobs($first:Int!,$after:String){ jobs(first:$first,after:$after){ nodes { id jobNumber title jobStatus } pageInfo { hasNextPage endCursor } } }";
 public static final String JOB_BILLING_SNAPSHOT="""
 query BillingSnapshot($id: EncodedId!) {
   job(id: $id) {
     id jobNumber title jobStatus total invoicedTotal uninvoicedTotal
     lineItems(first: 100) { nodes { id name quantity unitPrice } }
     visits(first: 100) {
       nodes {
         id title isComplete completedAt duration instructions
         lineItems(first: 100) { nodes { id name quantity unitPrice } }
         invoice { id invoiceNumber }
       }
     }
     invoices(first: 100) {
       nodes {
         id invoiceNumber invoiceStatus issuedDate
         lineItems(first: 100) { nodes { id name quantity unitPrice totalPrice } }
       }
     }
     timesheetEntries(first: 100) {
       nodes { id approved finalDuration labourRate label note visit { id } }
     }
     expenses(first: 100) {
       nodes { id title description total date }
     }
   }
 }
 """;
 private JobberQueries(){}
}
