package com.billingcopilot.provider.jobber;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.Map;

@Component
public class JobberGraphQlClient {
 private final RestClient client;
 private final String apiVersion;
 private final ObjectMapper mapper;
 public JobberGraphQlClient(RestClient.Builder builder, ObjectMapper mapper,
   @Value("${jobber.graphql-url:https://api.getjobber.com/api/graphql}") String url,
   @Value("${jobber.api-version:2026-05-12}") String apiVersion) {
   this.client=builder.baseUrl(url).build(); this.apiVersion=apiVersion; this.mapper=mapper;
 }
 public String execute(String accessToken, String query, Map<String,Object> variables){
  String response=client.post().contentType(MediaType.APPLICATION_JSON)
    .header("Authorization","Bearer "+accessToken)
    .header("X-JOBBER-GRAPHQL-VERSION",apiVersion)
    .body(Map.of("query",query,"variables",variables==null?Map.of():variables))
    .retrieve().body(String.class);
  if(response==null || response.isBlank()) throw new IllegalStateException("Empty Jobber GraphQL response");
  verifyGraphQlResponse(response);
  return response;
 }
 void verifyGraphQlResponse(String response){
  try {
   JsonNode root=mapper.readTree(response);
   JsonNode errors=root.path("errors");
   if(errors.isArray() && !errors.isEmpty()) {
    String message=errors.get(0).path("message").asText("Unknown GraphQL error");
    throw new IllegalStateException("Jobber GraphQL error: "+message);
   }
   if(root.path("data").isMissingNode() || root.path("data").isNull()) {
    throw new IllegalStateException("Jobber response contains no data");
   }
  } catch(com.fasterxml.jackson.core.JsonProcessingException ex) {
   throw new IllegalStateException("Invalid Jobber GraphQL JSON response",ex);
  }
 }
}
