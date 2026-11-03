package com.billingcopilot.integration.jobber;
import org.springframework.stereotype.Component;
import java.time.Instant;import java.util.concurrent.ConcurrentHashMap;
@Component public class PkceStore{
 public record Entry(Long tenantId,String verifier,Instant createdAt){}
 private final ConcurrentHashMap<String,Entry> byState=new ConcurrentHashMap<>();
 public void put(String state,Long tenantId,String verifier){byState.put(state,new Entry(tenantId,verifier,Instant.now()));}
 public Entry remove(String state){
  Entry value=byState.remove(state);
  if(value==null||value.createdAt().isBefore(Instant.now().minusSeconds(600)))return null;
  return value;
 }
}
