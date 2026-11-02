package com.billingcopilot.provider;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;
import java.math.BigDecimal;
import java.util.List;
@Component @Profile("!jobber") @Primary
public class SyntheticBillingDataProvider implements BillingDataProvider {
 public ProviderSnapshot load(String externalJobId) {
  return new ProviderSnapshot("demo-account", externalJobId, "1842",
    List.of(new WorkEvidence("VISIT","v1","Monthly maintenance",bd("1"),"completed"),
            new WorkEvidence("VISIT","v2","Salt application",bd("7"),"7 completed applications"),
            new WorkEvidence("TIMESHEET","t1","Emergency cleanup",bd("3"),"3 approved hours")),
    List.of(new InvoiceEvidence("inv1","l1","Monthly maintenance",bd("1"),bd("1400")),
            new InvoiceEvidence("inv1","l2","Salt application",bd("6"),bd("130")),
            new InvoiceEvidence("inv1","l3","Emergency cleanup",bd("3"),bd("150"))));
 }
 private static BigDecimal bd(String v){return new BigDecimal(v);}
}
