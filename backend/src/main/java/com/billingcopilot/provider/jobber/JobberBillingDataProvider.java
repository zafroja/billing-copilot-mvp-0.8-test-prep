package com.billingcopilot.provider.jobber;
import com.billingcopilot.integration.jobber.JobberTokenService;import com.billingcopilot.provider.BillingDataProvider;import com.billingcopilot.repository.JobberConnectionRepository;import com.fasterxml.jackson.databind.ObjectMapper;import org.springframework.context.annotation.Profile;import org.springframework.stereotype.Component;import java.util.Map;
@Component @Profile("jobber")
public class JobberBillingDataProvider implements BillingDataProvider {
 private final JobberTokenService tokens; private final JobberConnectionRepository connections; private final JobberGraphQlClient gql; private final JobberSnapshotMapper mapper; private final ObjectMapper om;
 public JobberBillingDataProvider(JobberTokenService tokens,JobberConnectionRepository connections,JobberGraphQlClient gql,JobberSnapshotMapper mapper,ObjectMapper om){this.tokens=tokens;this.connections=connections;this.gql=gql;this.mapper=mapper;this.om=om;}
 @Override public ProviderSnapshot load(String externalJobId){
  // MVP uses tenant 1 until authenticated tenant context is introduced.
  Long tenantId=1L; String token=tokens.validAccessToken(tenantId); var c=connections.findByTenantId(tenantId).orElseThrow();
  try{return mapper.map(c.getAccountId(),om.readTree(gql.execute(token,JobberQueries.JOB_BILLING_SNAPSHOT,Map.of("id",externalJobId))));}catch(Exception e){throw new IllegalStateException("Unable to load Jobber billing snapshot",e);}
 }
}
