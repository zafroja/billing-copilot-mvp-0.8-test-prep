package com.billingcopilot.model;
import java.math.BigDecimal;
import java.util.List;
public final class BillingModels {
  public record BillingRule(String service, BigDecimal rate, String unit) {}
  public record WorkItem(String service, BigDecimal quantity) {}
  public record InvoiceItem(String service, BigDecimal quantity, BigDecimal unitPrice) {}
  public record AuditRequest(List<BillingRule> rules, List<WorkItem> work, List<InvoiceItem> invoice) {}
  public record Discrepancy(String type, String service, String message, BigDecimal potentialImpact) {}
  public record AuditResult(List<Discrepancy> discrepancies, BigDecimal totalPotentialImpact) {}
  private BillingModels() {}
}
