package com.billingcopilot.dto;
import com.billingcopilot.model.BillingModels.*; import java.math.BigDecimal; import java.time.Instant; import java.util.*;
public final class AuditDtos {
 public record ProviderAuditRequest(UUID tenantId,List<BillingRule> rules){}
 public record ProviderAuditResponse(UUID auditId,String jobId,String jobNumber,AuditResult result){}
 public record AuditHistoryItem(UUID auditId,String externalJobId,Instant createdAt,BigDecimal potentialImpact){}
 public record EvidenceItem(String discrepancyType,String sourceType,String sourceId,String explanation,BigDecimal potentialImpact){}
 private AuditDtos(){}
}
