package com.billingcopilot.contract;

import java.math.BigDecimal;
import java.util.List;

public final class ContractDtos {
  public record SourceEvidence(int page, String quote) {}
  public record ExtractedBillingRule(String service, BigDecimal rate, String unit, SourceEvidence evidence, double confidence) {}
  public record ContractExtractionResult(String fileName, List<ExtractedBillingRule> rules, List<String> warnings) {}
  public record ReviewableBillingRule(long id, String service, BigDecimal rate, String unit, SourceEvidence evidence, double confidence, String reviewStatus) {}
  public record SavedContract(long contractId, String fileName, List<ReviewableBillingRule> rules, List<String> warnings) {}
  public record RuleReviewRequest(String status) {}
  private ContractDtos() {}
}
