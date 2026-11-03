package com.billingcopilot.provider.jobber;

import com.billingcopilot.model.BillingModels.*;
import com.billingcopilot.service.ReconciliationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class JobberRealSnapshotRegressionTest {
 private final ObjectMapper json=new ObjectMapper();
 private com.fasterxml.jackson.databind.JsonNode source() throws Exception {
  return json.readTree(getClass().getResourceAsStream("/fixtures/jobber-real-sanitized-job1.json"));
 }
 @Test void realGraphQlResponseDetectsSeventyFiveEuroReviewFinding() throws Exception {
  var original=source();
  assertEquals(0,original.path("data").path("job").path("completedAndUninvoicedVisitsCount").asInt());
  var snap=new JobberSnapshotMapper().map("test-account", original);
  assertEquals("FIXED_PRICE",snap.billingType());
  assertEquals(7,snap.work().size());
  assertEquals(1,snap.invoice().size());
  assertEquals(0,new BigDecimal("6").compareTo(snap.invoice().get(0).quantity()));
  assertEquals(0,new BigDecimal("75").compareTo(snap.invoice().get(0).unitPrice()));
  assertEquals(7,snap.work().stream().map(w->w.sourceId()).distinct().count());
  var rules=List.of(new BillingRule("Salt Application",new BigDecimal("75"),"application"));
  var input=new AuditRequest(rules,snap.work().stream().map(w->new WorkItem(w.service(),w.quantity())).toList(),
    snap.invoice().stream().map(i->new InvoiceItem(i.service(),i.quantity(),i.unitPrice())).toList());
  var audit=new ReconciliationService().audit(input);
  assertEquals(1,audit.discrepancies().size());
  assertEquals("QUANTITY_MISMATCH",audit.discrepancies().get(0).type());
  assertEquals(0,new BigDecimal("75.00").compareTo(audit.totalPotentialImpact()));
 }
 @Test void failClosedOnPartialVisits() throws Exception {
  var root=source();
  ((ObjectNode)root.path("data").path("job").path("visits").path("pageInfo")).put("hasNextPage",true);
  assertThrows(IllegalArgumentException.class,()->new JobberSnapshotMapper().map("test",root));
 }
 @Test void failClosedOnNonDraftInvoice() throws Exception {
  var root=source();
  ((ObjectNode)root.path("data").path("job").path("invoices").path("nodes").get(0)).put("invoiceStatus","voided");
  assertThrows(IllegalArgumentException.class,()->new JobberSnapshotMapper().map("test",root));
 }
 @Test void failClosedOnMissingInvoiceQuantity() throws Exception {
  var root=source();
  ((ObjectNode)root.path("data").path("job").path("invoices").path("nodes").get(0).path("lineItems").path("nodes").get(0)).remove("quantity");
  assertThrows(IllegalArgumentException.class,()->new JobberSnapshotMapper().map("test",root));
 }
 @Test void failClosedOnMultipleInvoices() throws Exception {
  var root=source();
  var nodes=(com.fasterxml.jackson.databind.node.ArrayNode)root.path("data").path("job").path("invoices").path("nodes");
  nodes.add(nodes.get(0).deepCopy());
  assertThrows(IllegalArgumentException.class,()->new JobberSnapshotMapper().map("test",root));
 }
}
