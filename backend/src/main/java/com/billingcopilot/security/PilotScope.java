package com.billingcopilot.security;
import java.util.UUID;
/** Deliberately single-tenant pilot; not a multi-tenant production release. */
public final class PilotScope {
 public static final long TENANT_ID=1L;
 public static final UUID AUDIT_TENANT=UUID.fromString("00000000-0000-0000-0000-000000000001");
 private PilotScope(){}
 public static void require(long tenant){if(tenant!=TENANT_ID) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN,"Tenant is not available");}
 public static void require(UUID tenant){if(!AUDIT_TENANT.equals(tenant))throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN,"Tenant is not available");}
}
