package com.billingcopilot.repository;
import com.billingcopilot.entity.AuditEvidence; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface AuditEvidenceRepository extends JpaRepository<AuditEvidence,UUID>{ List<AuditEvidence> findByAuditIdOrderById(UUID auditId); }
