package com.billingcopilot.contract;
import com.billingcopilot.contract.ContractDtos.*; import com.billingcopilot.entity.*; import com.billingcopilot.repository.*; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import org.springframework.web.multipart.MultipartFile; import java.io.IOException; import java.util.*;
@Service public class ContractService {
 private final ContractTextExtractor textExtractor; private final BillingRuleExtractor extractor; private final ContractDocumentRepository docs; private final ContractBillingRuleRepository rules;
 public ContractService(ContractTextExtractor t,BillingRuleExtractor e,ContractDocumentRepository d,ContractBillingRuleRepository r){textExtractor=t;extractor=e;docs=d;rules=r;}
 public ContractExtractionResult extract(MultipartFile file)throws IOException{return extractRaw(file);}
 @Transactional public SavedContract upload(long tenantId,MultipartFile file)throws IOException{com.billingcopilot.security.PilotScope.require(tenantId); var x=extractRaw(file); var doc=docs.save(new ContractDocument(tenantId,x.fileName())); List<ReviewableBillingRule> saved=new ArrayList<>(); for(var r:x.rules()){var e=rules.save(new ContractBillingRule(doc.getId(),r.service(),r.rate(),r.unit(),r.evidence().page(),r.evidence().quote(),r.confidence())); saved.add(dto(e));} return new SavedContract(doc.getId(),x.fileName(),saved,x.warnings());}
 @Transactional public ReviewableBillingRule review(long ruleId,String status){String s=status==null?"":status.trim().toUpperCase(Locale.ROOT); if(!Set.of("APPROVED","REJECTED","PENDING").contains(s)) throw new IllegalArgumentException("status must be APPROVED, REJECTED or PENDING"); var r=rules.findById(ruleId).orElseThrow(); requireOwnContract(r.getContractDocumentId()); r.review(s); return dto(rules.save(r));}
 public List<ReviewableBillingRule> list(long contractId){requireOwnContract(contractId); return rules.findByContractDocumentIdOrderById(contractId).stream().map(this::dto).toList();}
 public java.util.List<SavedContract> recentContracts(){
  return docs.findTop50ByTenantIdOrderByIdDesc(com.billingcopilot.security.PilotScope.TENANT_ID)
   .stream().map(d->new SavedContract(d.getId(),d.getFileName(),list(d.getId()),java.util.List.of())).toList();
 }
 @Transactional public SavedContract manualRate(ManualRateRequest req) {
  if(!req.confirmed())throw new IllegalArgumentException("Owner confirmation of the billing rate is required");
  if(req.service()==null||req.service().isBlank()||req.service().length()>160)throw new IllegalArgumentException("Valid service name is required");
  if(req.rate()==null||req.rate().signum()<=0||req.rate().scale()>2)throw new IllegalArgumentException("Positive currency rate with at most two decimals is required");
  if(req.unit()==null||!java.util.Set.of("application","visit","hour","month","unit").contains(req.unit().toLowerCase(java.util.Locale.ROOT)))
   throw new IllegalArgumentException("Choose a supported billing unit");
  if(req.source()==null||req.source().isBlank()||req.source().length()>1000)throw new IllegalArgumentException("Describe the source of the agreed rate");
  var doc=docs.save(new ContractDocument(com.billingcopilot.security.PilotScope.TENANT_ID,"Owner-entered rate sheet"));
  var rule=new ContractBillingRule(doc.getId(),req.service().trim(),req.rate(),req.unit().toLowerCase(java.util.Locale.ROOT),1,req.source().trim(),1.0);
  rule.review("APPROVED"); var saved=rules.save(rule);
  return new SavedContract(doc.getId(),"Owner-entered rate sheet",java.util.List.of(dto(saved)),java.util.List.of("Owner-entered rule: verify the contract allows per-unit billing before treating results as collectible revenue"));
 }
 public void requireOwnContract(long contractId) {var d=docs.findById(contractId).orElseThrow(()->new IllegalArgumentException("Unknown contract")); com.billingcopilot.security.PilotScope.require(d.getTenantId());}
 private ContractExtractionResult extractRaw(MultipartFile file)throws IOException{if(file.isEmpty())throw new IllegalArgumentException("Contract file is empty"); if(file.getSize()>10_000_000)throw new IllegalArgumentException("Contract exceeds 10 MB MVP limit"); String n=file.getOriginalFilename()==null?"contract.pdf":file.getOriginalFilename();
  n=n.replace('\\','/');n=n.substring(n.lastIndexOf('/')+1);
  if(n.length()>200||!n.toLowerCase(java.util.Locale.ROOT).endsWith(".pdf"))throw new IllegalArgumentException("PDF contracts only");
  byte[] payload=file.getBytes();
  if(payload.length<5||payload[0]!='%'||payload[1]!='P'||payload[2]!='D'||payload[3]!='F'||payload[4]!='-')throw new IllegalArgumentException("Invalid PDF file content");
  return extractor.extract(n,textExtractor.extract(payload));}
 private ReviewableBillingRule dto(ContractBillingRule r){return new ReviewableBillingRule(r.getId(),r.getService(),r.getRate(),r.getUnit(),new SourceEvidence(r.getSourcePage()==null?1:r.getSourcePage(),r.getSourceQuote()),r.getConfidence()==null?0:r.getConfidence(),r.getReviewStatus());}
}
