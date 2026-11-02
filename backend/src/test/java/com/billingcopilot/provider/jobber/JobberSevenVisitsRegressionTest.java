package com.billingcopilot.provider.jobber;

import com.billingcopilot.model.BillingModels.*;
import com.billingcopilot.service.ReconciliationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class JobberSevenVisitsRegressionTest {
 @Test void sevenCompletedVisitsAndSixInvoicedFlagsSeventyFiveEuros() throws Exception {
  var input=getClass().getResourceAsStream("/fixtures/jobber-seven-visits-six-invoiced.json");
  assertNotNull(input);
  var root=new ObjectMapper().readTree(input);
  var snapshot=new JobberSnapshotMapper().map("fixture-account",root);
  assertEquals(7,snapshot.work().size());
  assertEquals(1,snapshot.invoice().size());
  var request=new AuditRequest(
    List.of(new BillingRule("Salt Application",new BigDecimal("75"),"application")),
    snapshot.work().stream().map(w -> new WorkItem(w.service(),w.quantity())).toList(),
    snapshot.invoice().stream().map(i -> new InvoiceItem(i.service(),i.quantity(),i.unitPrice())).toList());
  var result=new ReconciliationService().audit(request);
  assertEquals(1,result.discrepancies().size());
  assertEquals("QUANTITY_MISMATCH",result.discrepancies().get(0).type());
  assertEquals(0,new BigDecimal("75.00").compareTo(result.totalPotentialImpact()));
 }
 @Test void expenseDoesNotBecomeBillableItem() throws Exception {
  var root=new ObjectMapper().readTree(getClass().getResourceAsStream("/fixtures/jobber-seven-visits-six-invoiced.json"));
  var job=root.path("data").path("job");
  ((com.fasterxml.jackson.databind.node.ObjectNode)job).set("expenses",new ObjectMapper().readTree("{\"nodes\":[{\"id\":\"expense-1\",\"title\":\"Fuel\",\"total\":300}]}"));
  var snapshot=new JobberSnapshotMapper().map("fixture-account",root);
  assertEquals(7,snapshot.work().size(),"Expense should not be counted as work");
 }
}
