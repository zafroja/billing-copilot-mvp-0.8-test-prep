package com.billingcopilot.provider.jobber;
import com.billingcopilot.provider.BillingDataProvider.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;import java.math.RoundingMode;import java.util.*;
@Component
public class JobberSnapshotMapper {
 public ProviderSnapshot map(String accountId, JsonNode root){
  JsonNode job=root.path("data").path("job"); if(job.isMissingNode()||job.isNull()) throw new IllegalArgumentException("Jobber job not found");
  List<WorkEvidence> work=new ArrayList<>(); List<InvoiceEvidence> invoices=new ArrayList<>();
  for(JsonNode visit:nodes(job.path("visits"))){ if(!visit.path("isComplete").asBoolean(false)) continue; for(JsonNode li:nodes(visit.path("lineItems"))) work.add(new WorkEvidence("VISIT",visit.path("id").asText(),text(li,"name","Unnamed service"),decimal(li,"quantity"),"Completed visit: "+text(visit,"title","")+" at "+text(visit,"completedAt","unknown time"))); }
  // Fixed-price/non-visit work can still be represented by job line items when no completed visit line items exist.
  if(work.isEmpty()) for(JsonNode li:nodes(job.path("lineItems"))) work.add(new WorkEvidence("JOB_LINE_ITEM",li.path("id").asText(),text(li,"name","Unnamed service"),decimal(li,"quantity"),"Job line item"));
  for(JsonNode inv:nodes(job.path("invoices"))) for(JsonNode li:nodes(inv.path("lineItems"))) invoices.add(new InvoiceEvidence(inv.path("id").asText(),li.path("id").asText(),text(li,"name","Unnamed service"),decimal(li,"quantity"),decimal(li,"unitPrice")));
  // Do not turn timesheets or expenses into billable work automatically.
  // They may indicate a review opportunity, but are not a valid quantity basis
  // without an approved contract rule and explicit billing mapping.
  return new ProviderSnapshot(accountId,job.path("id").asText(),job.path("jobNumber").asText(),work,invoices);
 }
 private List<JsonNode> nodes(JsonNode c){List<JsonNode> out=new ArrayList<>();JsonNode n=c.path("nodes");if(n.isArray())n.forEach(out::add);return out;}
 private String text(JsonNode n,String f,String fallback){String v=n.path(f).asText("");return v.isBlank()?fallback:v;}
 private BigDecimal decimal(JsonNode n,String f){JsonNode v=n.path(f);return v.isNumber()?v.decimalValue():new BigDecimal(v.asText("0"));}
}
