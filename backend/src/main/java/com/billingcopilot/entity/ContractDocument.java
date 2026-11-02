package com.billingcopilot.entity;
import jakarta.persistence.*; import java.time.OffsetDateTime;
@Entity @Table(name="contract_document")
public class ContractDocument {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(name="tenant_id",nullable=false) private Long tenantId;
 @Column(name="file_name",nullable=false) private String fileName;
 @Column(nullable=false) private String status="EXTRACTED";
 @Column(name="created_at",insertable=false,updatable=false) private OffsetDateTime createdAt;
 protected ContractDocument(){} public ContractDocument(Long tenantId,String fileName){this.tenantId=tenantId;this.fileName=fileName;}
 public Long getId(){return id;} public Long getTenantId(){return tenantId;} public String getFileName(){return fileName;} public String getStatus(){return status;}
}
