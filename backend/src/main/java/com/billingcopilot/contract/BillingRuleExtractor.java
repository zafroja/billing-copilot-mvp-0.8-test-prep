package com.billingcopilot.contract;
import com.billingcopilot.contract.ContractDtos.ContractExtractionResult;
public interface BillingRuleExtractor { ContractExtractionResult extract(String fileName, String contractText); }
