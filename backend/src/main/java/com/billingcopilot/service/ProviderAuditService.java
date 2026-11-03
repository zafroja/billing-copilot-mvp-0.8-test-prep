package com.billingcopilot.service;
import com.billingcopilot.dto.AuditDtos.*; import com.billingcopilot.entity.*; import com.billingcopilot.model.BillingModels.*; import com.billingcopilot.provider.BillingDataProvider; import com.billingcopilot.repository.*;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.util.*;
@Service
public class ProviderAuditService {
 private final BillingDataProvider provider; private final ReconciliationService reconciliation; private final BillingAuditRepository audits; private final AuditEvidenceRepository evidence; private final ContractDocumentRepository contracts; private final ContractBillingRuleRepository approvedRules;
 public ProviderAuditService(BillingDataProvider provider,ReconciliationService reconciliation,BillingAuditRepository audits,AuditEvidenceRepository evidence,ContractDocumentRepository contracts,ContractBillingRuleRepository approvedRules){this.provider=provider;this.reconciliation=reconciliation;this.audits=audits;this.evidence=evidence;this.contracts=contracts;this.approvedRules=approvedRules;}
 @Transactional public ProviderAuditResponse run(String externalJobId,ProviderAuditRequest request){
  if(request.contractId()==null) throw new IllegalArgumentException("An approved contract is required");
  // This is a single-tenant LOCAL prototype. Production requires server-authenticated tenant context.
  var doc=contracts.findById(request.contractId()).orElseThrow(()->new IllegalArgumentException("Unknown contract"));
  if(!Long.valueOf(1L).equals(doc.getTenantId()))throw new IllegalArgumentException("Contract does not belong to the local test tenant");
  var approved=approvedRules.findByContractDocumentIdAndReviewStatusOrderById(request.contractId(),"APPROVED")
    .stream().map(r->new BillingRule(r.getService(),r.getRate(),r.getUnit())).toList();
  if(approved.isEmpty())throw new IllegalArgumentException("Approve at least one stored billing rule before auditing");
  if(request.tenantId()!=null) com.billingcopilot.security.PilotScope.require(request.tenantId());
  var snap=provider.load(externalJobId);
  if("FIXED_PRICE".equalsIgnoreCase(snap.billingType())) {
   for(var workItem:snap.work()) {
    boolean approvedForWork=approved.stream().anyMatch(r -> ReconciliationService.sameService(r.service(),workItem.service())
      && r.unit()!=null && (r.unit().equalsIgnoreCase("application") || r.unit().equalsIgnoreCase("per application") || r.unit().equalsIgnoreCase("visit") || r.unit().equalsIgnoreCase("per visit")));
    if(!approvedForWork) throw new IllegalArgumentException("Fixed-price job requires an explicitly approved per-application/per-visit rule for "+workItem.service());
   }
  }
  var work=snap.work().stream().map(w->new WorkItem(w.service(),w.quantity())).toList();
  var invoice=snap.invoice().stream().map(i->new InvoiceItem(i.service(),i.quantity(),i.unitPrice())).toList();
  var result=reconciliation.audit(new AuditRequest(approved,work,invoice));
  var audit=audits.save(new BillingAudit(com.billingcopilot.security.PilotScope.AUDIT_TENANT,externalJobId,result.totalPotentialImpact()));
  for(var d:result.discrepancies()){
   var sources=snap.work().stream().filter(w->ReconciliationService.sameService(w.service(),d.service())).toList();
   if(sources.isEmpty()) evidence.save(new AuditEvidence(audit.getId(),d.type(),"INVOICE","unknown",d.message(),d.potentialImpact()));
   else {
    // Allocate monetary impact to the discrepancy once; evidence records are links, not separate losses.
    boolean first=true;
    for(var s:sources) {
     evidence.save(new AuditEvidence(audit.getId(),d.type(),s.sourceType(),s.sourceId(),d.message()+" | Evidence: "+s.note(),first?d.potentialImpact():java.math.BigDecimal.ZERO));
     first=false;
    }
   }
  }
  return new ProviderAuditResponse(audit.getId(),snap.jobId(),snap.jobNumber(),result);
 }
 public List<AuditHistoryItem> history(UUID tenantId){com.billingcopilot.security.PilotScope.require(tenantId);return audits.findTop50ByTenantIdOrderByCreatedAtDesc(tenantId).stream().map(a->new AuditHistoryItem(a.getId(),a.getExternalJobId(),a.getCreatedAt(),a.getPotentialImpact())).toList();}
 public List<EvidenceItem> evidence(UUID auditId){var audit=audits.findById(auditId).orElseThrow();com.billingcopilot.security.PilotScope.require(audit.getTenantId());return evidence.findByAuditIdOrderById(auditId).stream().map(e->new EvidenceItem(e.getDiscrepancyType(),e.getSourceType(),e.getSourceId(),e.getExplanation(),e.getPotentialImpact())).toList();}
}
