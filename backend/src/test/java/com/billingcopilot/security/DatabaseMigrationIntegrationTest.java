package com.billingcopilot.security;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import static org.junit.jupiter.api.Assertions.*;

/** Runs with the PostgreSQL service on CI, including Flyway + Hibernate validate on startup. */
@SpringBootTest
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named="RUN_DB_INTEGRATION", matches="true")
class DatabaseMigrationIntegrationTest {
 @Autowired JdbcTemplate jdbc;
 @Autowired MockMvc mvc;
 @Test void migrationsAndHibernateSchemaValidation() {
  assertEquals(1L, jdbc.queryForObject("SELECT count(*) FROM tenants WHERE id=1", Long.class));
  assertEquals(1L, jdbc.queryForObject("SELECT count(*) FROM information_schema.tables WHERE table_name='billing_audits'",Long.class));
  assertEquals(1L, jdbc.queryForObject("SELECT count(*) FROM information_schema.tables WHERE table_name='jobber_connection'",Long.class));
  assertEquals(4L, jdbc.queryForObject("SELECT count(*) FROM flyway_schema_history WHERE success=true",Long.class));
 }
 @Test void rejectsUnauthorizedAccessAndInvalidOauthState() throws Exception {
  assertEquals(401,mvc.perform(get("/api/session")).andReturn().getResponse().getStatus());
  var basic="Basic "+Base64.getEncoder().encodeToString("ci-owner:ci-test-only-unsafe-password".getBytes(StandardCharsets.UTF_8));
  assertEquals(200,mvc.perform(get("/api/session").header("Authorization",basic)).andReturn().getResponse().getStatus());
  assertEquals(400,mvc.perform(get("/api/integrations/jobber/callback").param("code","bogus").param("state","unknown"))
    .andReturn().getResponse().getStatus());
 }
}
