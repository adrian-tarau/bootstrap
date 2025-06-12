package net.microfalx.bootstrap.security.audit.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditRepository extends JpaRepository<Audit, Integer>, JpaSpecificationExecutor<Audit> {
}
