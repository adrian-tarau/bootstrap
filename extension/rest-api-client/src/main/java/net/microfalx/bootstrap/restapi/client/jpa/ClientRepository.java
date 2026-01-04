package net.microfalx.bootstrap.restapi.client.jpa;

import net.microfalx.bootstrap.jdbc.jpa.NaturalJpaRepository;
import org.springframework.stereotype.Repository;

@Repository("RestApiClientRepository")
public interface ClientRepository extends NaturalJpaRepository<Client, Integer> {
}
