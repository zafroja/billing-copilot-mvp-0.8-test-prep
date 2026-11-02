package com.billingcopilot.controller;
import com.billingcopilot.contract.ContractDtos.*; import com.billingcopilot.contract.ContractService; import org.springframework.http.MediaType; import org.springframework.web.bind.annotation.*; import org.springframework.web.multipart.MultipartFile; import java.io.IOException; import java.util.List;
@RestController @RequestMapping("/api/contracts") @CrossOrigin(origins="http://localhost:5173") public class ContractController { private final ContractService service; public ContractController(ContractService s){service=s;}
 @PostMapping(value="/extract",consumes=MediaType.MULTIPART_FORM_DATA_VALUE) public ContractExtractionResult extract(@RequestPart("file")MultipartFile f)throws IOException{return service.extract(f);}
 @PostMapping(value="/upload",consumes=MediaType.MULTIPART_FORM_DATA_VALUE) public SavedContract upload(@RequestParam(defaultValue="1") long tenantId,@RequestPart("file")MultipartFile f)throws IOException{return service.upload(tenantId,f);}
 @GetMapping("/{contractId}/rules") public List<ReviewableBillingRule> rules(@PathVariable long contractId){return service.list(contractId);}
 @PatchMapping("/rules/{ruleId}") public ReviewableBillingRule review(@PathVariable long ruleId,@RequestBody RuleReviewRequest r){return service.review(ruleId,r.status());}
}
