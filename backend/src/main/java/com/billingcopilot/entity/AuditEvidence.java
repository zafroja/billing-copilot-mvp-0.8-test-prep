package com.billingcopilot.entity;
import jakarta.persistence.*;
import java.math.BigDecimal; import java.util.UUID;
@Entity @Table(name="audit_evidence")
public class AuditEvidence {
 @Id private UUID id=UUID.randomUUID();
 @Column(nullable=false) private UUID auditId;
 @Column(nullable=false) private String discrepancyType;
 @Column(nullable=false) private String sourceType;
 private String sourceId;
 @Column(nullable=false,columnDefinition="text") private String explanation;
 @Column(nullable=false,precision=19,scale=2) private BigDecimal potentialImpact=BigDecimal.ZERO;
 protected AuditEvidence(){}
 public AuditEvidence(UUID auditId,String discrepancyType,String sourceType,String sourceId,String explanation,BigDecimal impact){this.auditId=auditId;this.discrepancyType=discrepancyType;this.sourceType=sourceType;this.sourceId=sourceId;this.explanation=explanation;this.potentialImpact=impact;}
 public UUID getId(){return id;} public UUID getAuditId(){return auditId;} public String getDiscrepancyType(){return discrepancyType;} public String getSourceType(){return sourceType;} public String getSourceId(){return sourceId;} public String getExplanation(){return explanation;} public BigDecimal getPotentialImpact(){return potentialImpact;}
}
