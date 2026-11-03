package com.billingcopilot.entity;
import jakarta.persistence.*;
import java.time.Instant;
@Entity
@Table(name="jobber_connection")
public class JobberConnection {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false) private Long tenantId;
 private String accountId; private String accountName;
 @Convert(converter=com.billingcopilot.security.EncryptedTokenConverter.class) @Column(columnDefinition="text") private String accessToken;
 @Convert(converter=com.billingcopilot.security.EncryptedTokenConverter.class) @Column(columnDefinition="text") private String refreshToken;
 private Instant accessTokenExpiresAt;
 @Column(nullable=false) private boolean connected;
 private Instant createdAt=Instant.now(); private Instant updatedAt=Instant.now();
 public Long getId(){return id;} public Long getTenantId(){return tenantId;} public void setTenantId(Long v){tenantId=v;}
 public String getAccountId(){return accountId;} public void setAccountId(String v){accountId=v;}
 public String getAccountName(){return accountName;} public void setAccountName(String v){accountName=v;}
 public String getAccessToken(){return accessToken;} public void setAccessToken(String v){accessToken=v;}
 public String getRefreshToken(){return refreshToken;} public void setRefreshToken(String v){refreshToken=v;}
 public Instant getAccessTokenExpiresAt(){return accessTokenExpiresAt;} public void setAccessTokenExpiresAt(Instant v){accessTokenExpiresAt=v;}
 public boolean isConnected(){return connected;} public void setConnected(boolean v){connected=v;}
 public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;} public void setUpdatedAt(Instant v){updatedAt=v;}
}
