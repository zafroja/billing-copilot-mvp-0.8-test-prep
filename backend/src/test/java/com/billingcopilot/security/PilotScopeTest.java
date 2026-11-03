package com.billingcopilot.security;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
class PilotScopeTest {
 @Test void ownerTenantIsAllowed(){assertDoesNotThrow(()->PilotScope.require(1L));assertDoesNotThrow(()->PilotScope.require(PilotScope.AUDIT_TENANT));}
 @Test void otherTenantsAreRejected(){assertThrows(ResponseStatusException.class,()->PilotScope.require(2L));assertThrows(ResponseStatusException.class,()->PilotScope.require(UUID.randomUUID()));}
}
