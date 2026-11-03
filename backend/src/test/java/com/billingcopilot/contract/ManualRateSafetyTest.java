package com.billingcopilot.contract;
import com.billingcopilot.contract.ContractDtos.ManualRateRequest;
import com.billingcopilot.repository.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class ManualRateSafetyTest {
 private ContractService service(){return new ContractService(mock(ContractTextExtractor.class),mock(BillingRuleExtractor.class),mock(ContractDocumentRepository.class),mock(ContractBillingRuleRepository.class));}
 @Test void manualRateRequiresExplicitConfirmation(){
  assertThrows(IllegalArgumentException.class,()->service().manualRate(new ManualRateRequest("Salt Application",new BigDecimal("75"),"application","Test agreement",false)));
 }
 @Test void negativeRateIsRejected(){
  assertThrows(IllegalArgumentException.class,()->service().manualRate(new ManualRateRequest("Salt Application",new BigDecimal("-75"),"application","Test agreement",true)));
 }
 @Test void uploadedFakePdfIsRejected() throws Exception {
  var file=new org.springframework.mock.web.MockMultipartFile("file","contract.pdf","application/pdf","not a PDF".getBytes());
  assertThrows(IllegalArgumentException.class,()->service().extract(file));
 }
 @Test void ownerMustDocumentRateSource(){
  assertThrows(IllegalArgumentException.class,()->service().manualRate(new ManualRateRequest("Salt Application",new BigDecimal("75"),"application","",true)));
 }
}
