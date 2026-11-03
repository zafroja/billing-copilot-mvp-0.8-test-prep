package com.billingcopilot.repository;
import com.billingcopilot.entity.ContractDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ContractDocumentRepository extends JpaRepository<ContractDocument,Long>{
 List<ContractDocument> findTop50ByTenantIdOrderByIdDesc(Long tenantId);
}
