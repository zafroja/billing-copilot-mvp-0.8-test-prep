package com.billingcopilot.repository;
import com.billingcopilot.entity.JobberConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface JobberConnectionRepository extends JpaRepository<JobberConnection,Long>{Optional<JobberConnection> findByTenantId(Long tenantId);}
