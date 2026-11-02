package com.billingcopilot.controller;
import com.billingcopilot.integration.jobber.JobberOAuthService;import org.springframework.http.ResponseEntity;import org.springframework.web.bind.annotation.*;import java.net.URI;import java.util.Map;
@RestController @RequestMapping("/api/integrations/jobber") @CrossOrigin(origins="http://localhost:5173")
public class JobberIntegrationController{
 private final JobberOAuthService service; public JobberIntegrationController(JobberOAuthService service){this.service=service;}
 @GetMapping("/status") public Map<String,Object> status(@RequestParam(defaultValue="1")Long tenantId){return service.status(tenantId);}
 @GetMapping("/connect") public Map<String,String> connect(@RequestParam(defaultValue="1")Long tenantId){return Map.of("authorizationUrl",service.authorizationUrl(tenantId));}
 @GetMapping("/callback") public ResponseEntity<Void> callback(@RequestParam String code,@RequestParam String state){service.handleCallback(code,state);return ResponseEntity.status(302).location(URI.create("http://localhost:5173/?jobber=connected")).build();}
}
