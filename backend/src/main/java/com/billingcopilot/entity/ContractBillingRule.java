package com.billingcopilot.entity;
import jakarta.persistence.*; import java.math.BigDecimal; import java.time.OffsetDateTime;
@Entity @Table(name="contract_billing_rule")
public class ContractBillingRule {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(name="contract_document_id",nullable=false) private Long contractDocumentId;
 @Column(nullable=false) private String service; @Column(nullable=false) private BigDecimal rate; @Column(nullable=false) private String unit;
 @Column(name="source_page") private Integer sourcePage; @Column(name="source_quote") private String sourceQuote; private Double confidence;
 @Column(name="review_status",nullable=false) private String reviewStatus="PENDING";
 @Column(name="human_verified",nullable=false) private boolean humanVerified=false;
 @Column(name="created_at",insertable=false,updatable=false) private OffsetDateTime createdAt;
 protected ContractBillingRule(){}
 public ContractBillingRule(Long contractId,String service,BigDecimal rate,String unit,Integer page,String quote,Double confidence){this.contractDocumentId=contractId;this.service=service;this.rate=rate;this.unit=unit;this.sourcePage=page;this.sourceQuote=quote;this.confidence=confidence;}
 public void review(String status){this.reviewStatus=status;this.humanVerified=!"PENDING".equals(status);}
 public Long getId(){return id;} public Long getContractDocumentId(){return contractDocumentId;} public String getService(){return service;} public BigDecimal getRate(){return rate;} public String getUnit(){return unit;} public Integer getSourcePage(){return sourcePage;} public String getSourceQuote(){return sourceQuote;} public Double getConfidence(){return confidence;} public String getReviewStatus(){return reviewStatus;}
}
