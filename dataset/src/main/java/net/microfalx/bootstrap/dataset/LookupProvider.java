package net.microfalx.bootstrap.dataset;

import net.microfalx.bootstrap.model.Filter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * An interface used to provide models for lookups.
 *
 * @param <M> the model identifier
 */
public interface LookupProvider<M> {

    /**
     * Returns the model for this provider.
     *
     * @return a non-null instance
     */
    Class<M> getModel();

    /**
     * Extracts all models for this lookup.
     *
     * @return a non-null instance
     */
    Iterable<M> extractAll();

    /**
     * Extracts records based on a filter.
     *
     * @param filterable the filter information
     * @return a page of mode;s
     */
    Page<M> extract(Pageable pageable, Filter filterable);
}
