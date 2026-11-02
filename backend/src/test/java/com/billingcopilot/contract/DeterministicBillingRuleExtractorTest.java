package com.billingcopilot.contract;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
class DeterministicBillingRuleExtractorTest {
 @Test void extractsSimpleRateSheetClausesWithEvidence(){
  var x=new DeterministicBillingRuleExtractor().extract("sample.pdf", "Monthly maintenance: $1400 per month\nSalt application: $130 per application\nEmergency cleanup: $165 per hour");
  assertThat(x.rules()).hasSize(3); assertThat(x.rules().get(1).service()).isEqualTo("Salt application"); assertThat(x.rules().get(1).rate()).isEqualByComparingTo("130"); assertThat(x.rules().get(1).evidence().quote()).contains("$130");
 }
}
