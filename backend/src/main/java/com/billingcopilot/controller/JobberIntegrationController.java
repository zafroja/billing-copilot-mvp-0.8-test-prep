package com.billingcopilot.controller;
import com.billingcopilot.integration.jobber.JobberOAuthService;
import com.billingcopilot.security.PilotScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.Map;
@RestController @RequestMapping("/api/integrations/jobber")
public class JobberIntegrationController {
 private final JobberOAuthService service;
 private final String frontendOrigin;
 public JobberIntegrationController(JobberOAuthService service,
   @Value("${app.frontend-origin:http://localhost:5173}") String frontendOrigin){this.service=service;this.frontendOrigin=frontendOrigin;}
 @GetMapping("/status") public Map<String,Object> status(){return service.status(PilotScope.TENANT_ID);}
 @GetMapping("/connect") public Map<String,String> connect(){return Map.of("authorizationUrl",service.authorizationUrl(PilotScope.TENANT_ID));}
 @GetMapping("/callback") public ResponseEntity<Void> callback(@RequestParam String code,@RequestParam String state){
  service.handleCallback(code,state);
  return ResponseEntity.status(302).location(URI.create(frontendOrigin+"/?jobber=connected")).build();
 }
}
