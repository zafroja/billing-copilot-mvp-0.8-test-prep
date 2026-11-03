package com.billingcopilot.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

@Configuration
public class PilotSecurityConfig {
 @Bean UserDetailsService pilotOwner(
   @Value("${pilot.username:}") String username,
   @Value("${pilot.password-bcrypt:}") String bcryptHash) {
  if(username.isBlank() || !bcryptHash.matches("\\$2[aby]\\$[0-9]{2}\\$[./A-Za-z0-9]{53}"))
   throw new IllegalStateException("PILOT_USERNAME and PILOT_PASSWORD_BCRYPT (bcrypt hash) are required");
  return new InMemoryUserDetailsManager(User.withUsername(username)
    .password("{bcrypt}"+bcryptHash).roles("OWNER").build());
 }
 @Bean SecurityFilterChain filter(HttpSecurity http) throws Exception {
  http.csrf(csrf -> csrf.disable()) // Basic header auth; no browser session/cookie credentials.
   .headers(h -> h.contentTypeOptions(Customizer.withDefaults()).frameOptions(f -> f.deny()))
   .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
   .cors(Customizer.withDefaults())
   .authorizeHttpRequests(a -> a
     .requestMatchers(HttpMethod.GET,"/api/integrations/jobber/callback").permitAll()
     .requestMatchers(HttpMethod.GET,"/api/health","/","/index.html","/assets/**","/favicon.ico","/robots.txt","/error").permitAll()
     .anyRequest().hasRole("OWNER"))
   .httpBasic(Customizer.withDefaults())
   .formLogin(f -> f.disable());
  return http.build();
 }
 @Bean CorsConfigurationSource cors(@Value("${app.frontend-origin:http://localhost:5173}") String origin) {
  CorsConfiguration c=new CorsConfiguration();
  c.setAllowedOrigins(List.of(origin));
  c.setAllowedMethods(List.of("GET","POST","PATCH","OPTIONS"));
  c.setAllowedHeaders(List.of("Authorization","Content-Type"));
  c.setAllowCredentials(false);
  UrlBasedCorsConfigurationSource src=new UrlBasedCorsConfigurationSource();src.registerCorsConfiguration("/api/**",c);return src;
 }
}
