package com.billingcopilot.controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Value;
import java.util.Map;
@RestController @RequestMapping("/api/session")
public class OwnerSessionController {
 @GetMapping public Map<String,Object> info(@Value("${pilot.currency:EUR}") String currency) {
  return Map.of("authenticated",true,"pilotMode",true,"currency",currency);
 }
}
