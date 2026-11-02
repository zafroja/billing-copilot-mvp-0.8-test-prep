package com.billingcopilot.integration.jobber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
@Component
public class JobberOAuthProperties {
 public final String clientId,clientSecret,redirectUri,authorizeUrl,tokenUrl;
 public JobberOAuthProperties(@Value("${jobber.oauth.client-id:}")String clientId,@Value("${jobber.oauth.client-secret:}")String clientSecret,@Value("${jobber.oauth.redirect-uri:http://localhost:8080/api/integrations/jobber/callback}")String redirectUri,@Value("${jobber.oauth.authorize-url:https://api.getjobber.com/api/oauth/authorize}")String authorizeUrl,@Value("${jobber.oauth.token-url:https://api.getjobber.com/api/oauth/token}")String tokenUrl){this.clientId=clientId;this.clientSecret=clientSecret;this.redirectUri=redirectUri;this.authorizeUrl=authorizeUrl;this.tokenUrl=tokenUrl;}
 public boolean configured(){return !clientId.isBlank()&&!clientSecret.isBlank();}
}
