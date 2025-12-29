package net.microfalx.bootstrap.restapi.client.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository("RestApiAuditRepository")
public interface AuditRepository extends JpaRepository<Audit, Integer>, JpaSpecificationExecutor<Audit> {
}
