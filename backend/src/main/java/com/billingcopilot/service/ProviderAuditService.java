package com.billingcopilot.service;
import com.billingcopilot.dto.AuditDtos.*; import com.billingcopilot.entity.*; import com.billingcopilot.model.BillingModels.*; import com.billingcopilot.provider.BillingDataProvider; import com.billingcopilot.repository.*;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.util.*;
@Service
public class ProviderAuditService {
 private final BillingDataProvider provider; private final ReconciliationService reconciliation; private final BillingAuditRepository audits; private final AuditEvidenceRepository evidence;
 public ProviderAuditService(BillingDataProvider provider,ReconciliationService reconciliation,BillingAuditRepository audits,AuditEvidenceRepository evidence){this.provider=provider;this.reconciliation=reconciliation;this.audits=audits;this.evidence=evidence;}
 @Transactional public ProviderAuditResponse run(String externalJobId,ProviderAuditRequest request){
  var snap=provider.load(externalJobId);
  var work=snap.work().stream().map(w->new WorkItem(w.service(),w.quantity())).toList();
  var invoice=snap.invoice().stream().map(i->new InvoiceItem(i.service(),i.quantity(),i.unitPrice())).toList();
  var result=reconciliation.audit(new AuditRequest(request.rules(),work,invoice));
  var audit=audits.save(new BillingAudit(request.tenantId(),externalJobId,result.totalPotentialImpact()));
  for(var d:result.discrepancies()){
   var sources=snap.work().stream().filter(w->ReconciliationService.sameService(w.service(),d.service())).toList();
   if(sources.isEmpty()) evidence.save(new AuditEvidence(audit.getId(),d.type(),"INVOICE","unknown",d.message(),d.potentialImpact()));
   else for(var s:sources) evidence.save(new AuditEvidence(audit.getId(),d.type(),s.sourceType(),s.sourceId(),d.message()+" | Evidence: "+s.note(),d.potentialImpact()));
  }
  return new ProviderAuditResponse(audit.getId(),snap.jobId(),snap.jobNumber(),result);
 }
 public List<AuditHistoryItem> history(UUID tenantId){return audits.findTop50ByTenantIdOrderByCreatedAtDesc(tenantId).stream().map(a->new AuditHistoryItem(a.getId(),a.getExternalJobId(),a.getCreatedAt(),a.getPotentialImpact())).toList();}
 public List<EvidenceItem> evidence(UUID auditId){return evidence.findByAuditIdOrderById(auditId).stream().map(e->new EvidenceItem(e.getDiscrepancyType(),e.getSourceType(),e.getSourceId(),e.getExplanation(),e.getPotentialImpact())).toList();}
}
