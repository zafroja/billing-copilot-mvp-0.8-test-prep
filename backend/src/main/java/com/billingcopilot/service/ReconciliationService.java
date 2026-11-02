package com.billingcopilot.service;
import com.billingcopilot.model.BillingModels.*; import org.springframework.stereotype.Service; import java.math.BigDecimal; import java.util.*;
@Service public class ReconciliationService {
 private record InvoiceAggregate(BigDecimal quantity, BigDecimal weightedTotal, BigDecimal representativeRate){}
 public AuditResult audit(AuditRequest req){
  Map<String,BillingRule> rules=new HashMap<>(); for(var r:safe(req.rules())) rules.put(norm(r.service()),r);
  Map<String,BigDecimal> work=new HashMap<>(); for(var w:safe(req.work())) work.merge(norm(w.service()),w.quantity(),BigDecimal::add);
  Map<String,List<InvoiceItem>> grouped=new HashMap<>(); for(var i:safe(req.invoice())) grouped.computeIfAbsent(norm(i.service()),k->new ArrayList<>()).add(i);
  List<Discrepancy> out=new ArrayList<>();
  for(var e:work.entrySet()){
   String key=e.getKey(); BigDecimal actual=e.getValue(); BillingRule rule=rules.get(key); List<InvoiceItem> lines=grouped.getOrDefault(key,List.of());
   if(lines.isEmpty()){BigDecimal impact=rule==null?BigDecimal.ZERO:actual.multiply(rule.rate()); out.add(new Discrepancy("MISSING_ITEM",display(key),"Completed work has no matching invoice line",money(impact))); continue;}
   var agg=aggregate(lines); BigDecimal invoicedQty=agg.quantity();
   if(actual.compareTo(invoicedQty)!=0){BigDecimal rate=rule!=null?rule.rate():agg.representativeRate(); BigDecimal impact=actual.subtract(invoicedQty).abs().multiply(rate); out.add(new Discrepancy("QUANTITY_MISMATCH",display(key),"Work quantity "+actual+" vs invoice quantity "+invoicedQty,money(impact)));}
   if(rule!=null){
    BigDecimal expectedForInvoicedQty=rule.rate().multiply(invoicedQty);
    if(expectedForInvoicedQty.compareTo(agg.weightedTotal())!=0){BigDecimal impact=expectedForInvoicedQty.subtract(agg.weightedTotal()).abs(); out.add(new Discrepancy("RATE_MISMATCH",display(key),"Contract rate "+rule.rate()+" vs invoice effective amount "+agg.weightedTotal()+" for quantity "+invoicedQty,money(impact)));}
   }
  }
  BigDecimal total=out.stream().map(Discrepancy::potentialImpact).reduce(BigDecimal.ZERO,BigDecimal::add); return new AuditResult(List.copyOf(out),money(total));
 }
 private InvoiceAggregate aggregate(List<InvoiceItem> lines){BigDecimal q=BigDecimal.ZERO,total=BigDecimal.ZERO; for(var i:lines){q=q.add(i.quantity());total=total.add(i.quantity().multiply(i.unitPrice()));} BigDecimal rate=lines.get(0).unitPrice(); return new InvoiceAggregate(q,total,rate);}
 private static <T> List<T> safe(List<T> x){return x==null?List.of():x;}
 private static BigDecimal money(BigDecimal x){return x.setScale(2,java.math.RoundingMode.HALF_UP);}
 public static boolean sameService(String a,String b){return normalize(a).equals(normalize(b));}
 private static String normalize(String s){return s==null?"":s.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+"," ").trim();}
 private String norm(String s){return normalize(s);} private String display(String s){return Arrays.stream(s.split(" ")).filter(x->!x.isBlank()).map(x->Character.toUpperCase(x.charAt(0))+x.substring(1)).reduce((a,b)->a+" "+b).orElse(s);}
}
