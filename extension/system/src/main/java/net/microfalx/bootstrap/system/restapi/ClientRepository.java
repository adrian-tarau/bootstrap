package net.microfalx.bootstrap.system.restapi;

import net.microfalx.bootstrap.jdbc.jpa.NaturalJpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository("RestApiClientAdminRepository")
public interface ClientRepository extends NaturalJpaRepository<Client, Integer>, JpaSpecificationExecutor<Client> {
}
