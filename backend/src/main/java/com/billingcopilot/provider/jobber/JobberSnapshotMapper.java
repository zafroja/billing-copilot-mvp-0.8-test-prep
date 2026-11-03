package com.billingcopilot.provider.jobber;

import com.billingcopilot.provider.BillingDataProvider.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.*;

@Component
public class JobberSnapshotMapper {
 public ProviderSnapshot map(String accountId, JsonNode root) {
  JsonNode errors=root.path("errors");
  if(errors.isArray() && !errors.isEmpty()) throw new IllegalArgumentException("Jobber snapshot contains GraphQL errors");
  JsonNode job=root.path("data").path("job");
  if(job.isMissingNode()||job.isNull()) throw new IllegalArgumentException("Jobber job not found");
  // Fail closed: a partial response could conceal billable visits or invoice lines.
  requireAllPages(job.path("visits"),"visits");
  requireAllPages(job.path("invoices"),"invoices");
  List<JsonNode> visits=nodes(job.path("visits"));
  List<JsonNode> invoicesNodes=nodes(job.path("invoices"));
  if(invoicesNodes.isEmpty()) throw new IllegalArgumentException("No linked invoice exists; billing audit requires one draft invoice");
  if(invoicesNodes.size()>1) throw new IllegalArgumentException("Multiple invoices require explicit audit scope; cannot safely sum across invoices yet");
  if(!"draft".equalsIgnoreCase(invoicesNodes.get(0).path("invoiceStatus").asText()))
   throw new IllegalArgumentException("Private pilot audits only draft invoices to avoid historical or voided adjustments");
  List<WorkEvidence> work=new ArrayList<>();
  for(JsonNode visit:visits) {
   if(!visit.path("isComplete").asBoolean(false)) continue;
   if(visit.path("id").asText().isBlank()) throw new IllegalArgumentException("Completed visit is missing its ID");
   var items=nodes(visit.path("lineItems"));
   if(items.size()>=10) throw new IllegalArgumentException("Visit line items may be truncated; paginate before auditing");
   for(JsonNode li:items) work.add(new WorkEvidence("VISIT", visit.path("id").asText(), text(li,"name","Unnamed service"),
     decimal(li,"quantity"), "Completed visit: "+text(visit,"title","")+" at "+text(visit,"completedAt","unknown time")));
  }
  // Do not infer billable quantities from job-level lines, timesheets or expenses.
  // In particular, a fixed-price job can contain many visits without per-visit charges.
  List<InvoiceEvidence> invoiced=new ArrayList<>();
  for(JsonNode inv:invoicesNodes) {
   var lines=nodes(inv.path("lineItems"));
   if(lines.size()>=10) throw new IllegalArgumentException("Invoice lines may be truncated; paginate before auditing");
   for(JsonNode li:lines) invoiced.add(new InvoiceEvidence(inv.path("id").asText(),li.path("id").asText(),text(li,"name","Unnamed service"),decimal(li,"quantity"),decimal(li,"unitPrice")));
  }
  return new ProviderSnapshot(accountId,job.path("id").asText(),job.path("jobNumber").asText(),work,invoiced,job.path("billingType").asText("UNKNOWN"));
 }
 private void requireAllPages(JsonNode connection,String field){
  JsonNode info=connection.path("pageInfo");
  if(info.isMissingNode() || info.isNull()) throw new IllegalArgumentException("Missing "+field+" pagination metadata");
  if(info.path("hasNextPage").asBoolean(false)) throw new IllegalArgumentException("More "+field+" exist; pagination must complete before auditing");
 }
 private List<JsonNode> nodes(JsonNode c){List<JsonNode> out=new ArrayList<>();JsonNode n=c.path("nodes");if(n.isArray())n.forEach(out::add);return out;}
 private String text(JsonNode n,String field,String fallback){String s=n.path(field).asText("");return s.isBlank()?fallback:s;}
 private BigDecimal decimal(JsonNode n,String field){
  JsonNode v=n.path(field);
  if(!v.isNumber())throw new IllegalArgumentException("Missing or non-numeric Jobber "+field+"; cannot safely audit");
  BigDecimal value=v.decimalValue();
  if(value.signum()<0)throw new IllegalArgumentException("Negative Jobber "+field+" requires manual review");
  return value;
 }
}
