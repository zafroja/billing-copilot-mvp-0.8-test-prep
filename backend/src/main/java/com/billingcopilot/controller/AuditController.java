package com.billingcopilot.controller;
import com.billingcopilot.dto.AuditDtos.*; import com.billingcopilot.model.BillingModels.*; import com.billingcopilot.service.*; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @RequestMapping("/api/audits") @CrossOrigin(origins="http://localhost:5173")
public class AuditController {
 private final ReconciliationService reconciliation; private final ProviderAuditService providerAudits; private final ContractAuditService contractAudits;
 public AuditController(ReconciliationService reconciliation,ProviderAuditService providerAudits,ContractAuditService contractAudits){this.reconciliation=reconciliation;this.providerAudits=providerAudits;this.contractAudits=contractAudits;}
 @PostMapping public AuditResult audit(@RequestBody AuditRequest request){return reconciliation.audit(request);}
 public record ContractAuditRequest(List<WorkItem> work,List<InvoiceItem> invoice){}
 @PostMapping("/contracts/{contractId}") public AuditResult auditContract(@PathVariable long contractId,@RequestBody ContractAuditRequest request){return contractAudits.audit(contractId,request.work(),request.invoice());}
 @PostMapping("/jobs/{jobId}") public ProviderAuditResponse auditProviderJob(@PathVariable String jobId,@RequestBody ProviderAuditRequest request){return providerAudits.run(jobId,request);}
 @GetMapping("/history") public List<AuditHistoryItem> history(@RequestParam UUID tenantId){return providerAudits.history(tenantId);}
 @GetMapping("/{auditId}/evidence") public List<EvidenceItem> evidence(@PathVariable UUID auditId){return providerAudits.evidence(auditId);}
}
