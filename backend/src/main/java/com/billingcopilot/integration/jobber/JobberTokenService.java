package com.billingcopilot.integration.jobber;
import com.billingcopilot.entity.JobberConnection;
import com.billingcopilot.repository.JobberConnectionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import java.time.Instant;
@Service
public class JobberTokenService {
 private final JobberOAuthProperties p; private final JobberConnectionRepository repo; private final ObjectMapper om; private final RestClient rest=RestClient.create();
 public JobberTokenService(JobberOAuthProperties p,JobberConnectionRepository repo,ObjectMapper om){this.p=p;this.repo=repo;this.om=om;}
 public String validAccessToken(Long tenantId){
  JobberConnection c=repo.findByTenantId(tenantId).filter(JobberConnection::isConnected).orElseThrow(()->new IllegalStateException("Jobber is not connected for tenant "+tenantId));
  if(c.getAccessTokenExpiresAt()!=null && c.getAccessTokenExpiresAt().isAfter(Instant.now().plusSeconds(120))) return c.getAccessToken();
  return refresh(c);
 }
 private synchronized String refresh(JobberConnection c){
  // Re-read after acquiring the JVM lock: a preceding refresh may have rotated the token.
  c=repo.findByTenantId(c.getTenantId()).filter(JobberConnection::isConnected)
    .orElseThrow(()->new IllegalStateException("Jobber connection no longer active"));
  if(c.getAccessTokenExpiresAt()!=null && c.getAccessTokenExpiresAt().isAfter(Instant.now().plusSeconds(120))) return c.getAccessToken();
  MultiValueMap<String,String> form=new LinkedMultiValueMap<>(); form.add("client_id",p.clientId); form.add("client_secret",p.clientSecret); form.add("grant_type","refresh_token"); form.add("refresh_token",c.getRefreshToken());
  try{
   String body=rest.post().uri(p.tokenUrl).contentType(MediaType.APPLICATION_FORM_URLENCODED).body(form).retrieve().body(String.class);
   JsonNode n=om.readTree(body); c.setAccessToken(n.path("access_token").asText());
   String rotated=n.path("refresh_token").asText(""); if(!rotated.isBlank()) c.setRefreshToken(rotated);
   c.setAccessTokenExpiresAt(Instant.now().plusSeconds(n.path("expires_in").asLong(3600))); c.setUpdatedAt(Instant.now()); repo.save(c); return c.getAccessToken();
  }catch(org.springframework.web.client.HttpClientErrorException e){
   // Invalidate only on an explicit rejected refresh token, not on transient outages.
   if(e.getStatusCode().value()==400 || e.getStatusCode().value()==401){
    c.setConnected(false);c.setUpdatedAt(Instant.now());repo.save(c);
   }
   throw new IllegalStateException("Jobber token refresh rejected; review integration status",e);
  }catch(Exception e){throw new IllegalStateException("Temporary Jobber token refresh failure; retry later",e);}
 }
}
