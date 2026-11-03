package com.billingcopilot.integration.jobber;
import com.billingcopilot.security.TokenCrypto;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
@Component @Profile("jobber")
public class JobberSecretsGuard {
 private final JobberOAuthProperties oauth;
 public JobberSecretsGuard(JobberOAuthProperties oauth){this.oauth=oauth;}
 @PostConstruct void verify(){
  if(!oauth.configured())throw new IllegalStateException("Jobber profile requires client ID and client secret");
  // Force AES key validation before the first OAuth exchange.
  String encrypted=TokenCrypto.encrypt("startup-check");
  if(!"startup-check".equals(TokenCrypto.decrypt(encrypted)))throw new IllegalStateException("Invalid OAuth encryption key");
 }
}
