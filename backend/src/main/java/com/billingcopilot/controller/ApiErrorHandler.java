package com.billingcopilot.controller;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;
@RestControllerAdvice
public class ApiErrorHandler {
 @ExceptionHandler(IllegalArgumentException.class)
 public ResponseEntity<Map<String,String>> invalid(IllegalArgumentException e) {
  return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()==null?"Invalid request":e.getMessage()));
 }
 @ExceptionHandler(IllegalStateException.class)
 public ResponseEntity<Map<String,String>> state(IllegalStateException e) {
  // Avoid exposing OAuth secrets or exception causes.
  return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Operation unavailable: check setup, approvals and integration state"));
 }
}
