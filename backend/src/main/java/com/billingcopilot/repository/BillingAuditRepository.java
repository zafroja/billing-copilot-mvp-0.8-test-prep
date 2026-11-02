package com.billingcopilot.repository;
import com.billingcopilot.entity.BillingAudit; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface BillingAuditRepository extends JpaRepository<BillingAudit,UUID>{ List<BillingAudit> findTop50ByTenantIdOrderByCreatedAtDesc(UUID tenantId); }
