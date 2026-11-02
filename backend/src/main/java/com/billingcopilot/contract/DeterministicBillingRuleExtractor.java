package com.billingcopilot.contract;

import com.billingcopilot.contract.ContractDtos.*;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.*;

@Component
public class DeterministicBillingRuleExtractor implements BillingRuleExtractor {
  private static final Pattern RATE = Pattern.compile("(?im)^\\s*([A-Za-z][A-Za-z0-9 /&_-]{2,60}?)\\s*(?:[:=-]|is|at|billed at|will be billed at)\\s*\\$?([0-9]+(?:\\.[0-9]{1,2})?)\\s*(?:/|per\\s+)(application|visit|hour|hr|month|service|push|unit)s?\\b.*$");
  @Override public ContractExtractionResult extract(String fileName, String text) {
    List<ExtractedBillingRule> rules = new ArrayList<>(); List<String> warnings = new ArrayList<>();
    Matcher m = RATE.matcher(text == null ? "" : text);
    while (m.find()) {
      String service=m.group(1).trim(); BigDecimal rate=new BigDecimal(m.group(2)); String unit=normalizeUnit(m.group(3));
      String line=m.group(0).trim(); int page=approxPage(text,m.start());
      rules.add(new ExtractedBillingRule(service,rate,unit,new SourceEvidence(page,line),0.95));
    }
    if (rules.isEmpty()) warnings.add("No deterministic billing-rate clauses found. AI extraction can be enabled later for complex language.");
    return new ContractExtractionResult(fileName,List.copyOf(rules),List.copyOf(warnings));
  }
  private static String normalizeUnit(String u){u=u.toLowerCase(Locale.ROOT); return u.equals("hr")?"hour":u;}
  private static int approxPage(String text,int offset){int pages=1; for(int i=0;i<Math.min(offset,text.length());i++) if(text.charAt(i)=='\f') pages++; return pages;}
}
