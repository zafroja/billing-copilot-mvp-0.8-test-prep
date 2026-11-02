package com.billingcopilot.entity;
import jakarta.persistence.*;
import java.math.BigDecimal; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="billing_audits")
public class BillingAudit {
 @Id private UUID id=UUID.randomUUID();
 @Column(nullable=false) private UUID tenantId;
 @Column(nullable=false) private String externalJobId;
 @Column(nullable=false) private Instant createdAt=Instant.now();
 @Column(nullable=false,precision=19,scale=2) private BigDecimal potentialImpact=BigDecimal.ZERO;
 protected BillingAudit(){}
 public BillingAudit(UUID tenantId,String externalJobId,BigDecimal impact){this.tenantId=tenantId;this.externalJobId=externalJobId;this.potentialImpact=impact;}
 public UUID getId(){return id;} public UUID getTenantId(){return tenantId;} public String getExternalJobId(){return externalJobId;} public Instant getCreatedAt(){return createdAt;} public BigDecimal getPotentialImpact(){return potentialImpact;}
}
