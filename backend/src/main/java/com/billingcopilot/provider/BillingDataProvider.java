package com.billingcopilot.provider;
import java.math.BigDecimal;
import java.util.List;
public interface BillingDataProvider {
  ProviderSnapshot load(String externalJobId);
  record ProviderSnapshot(String accountId, String jobId, String jobNumber, List<WorkEvidence> work, List<InvoiceEvidence> invoice) {}
  record WorkEvidence(String sourceType, String sourceId, String service, BigDecimal quantity, String note) {}
  record InvoiceEvidence(String invoiceId, String lineId, String service, BigDecimal quantity, BigDecimal unitPrice) {}
}
