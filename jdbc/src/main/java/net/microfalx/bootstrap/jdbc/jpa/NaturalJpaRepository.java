package net.microfalx.bootstrap.jdbc.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

/**
 * An interface for a JPA repository which knows how to find an entity by its natural identifier.
 *
 * @param <T>  the entity type
 * @param <ID> the identifier type
 */
@NoRepositoryBean
public interface NaturalJpaRepository<T, ID> extends JpaRepository<T, ID> {

    /**
     * Retrieves an entity by its natural id.
     *
     * @param id must not be {@literal null}.
     * @return the entity with the given id or {@literal Optional#empty()} if none found.
     * @throws IllegalArgumentException if {@literal id} is {@literal null}.
     */
    Optional<T> findByNaturalId(String id);
}
