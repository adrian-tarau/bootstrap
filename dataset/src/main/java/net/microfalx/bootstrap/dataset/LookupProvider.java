package net.microfalx.bootstrap.dataset;

import net.microfalx.bootstrap.model.Filter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * An interface used to provide models for lookups.
 *
 * @param <M> the model identifier
 */
public interface LookupProvider<M extends Lookup<ID>, ID> {

    /**
     * Returns the model for this provider.
     *
     * @return a non-null instance
     */
    Class<M> getModel();

    /**
     * Finds a lookup by its identifier.
     *
     * @param id the identifier
     * @return an optional lookup
     */
    Optional<M> findById(ID id);

    /**
     * Returns all lookups, sorted by name.
     *
     * @return a non-null instance
     */
    Iterable<M> findAll();

    /**
     * Returns all lookups, sorted by name.
     *
     * @return a non-null instance
     */
    Iterable<M> findAll(Pageable pageable);

    /**
     * Returns a page of lookups based on page information and a filter.
     *
     * @param pageable   the page information
     * @param filterable the filter information
     * @return a page of mode;s
     */
    Page<M> findAll(Pageable pageable, Filter filterable);
}
