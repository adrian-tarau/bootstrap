package net.microfalx.bootstrap.web.dataset;

import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.model.Filter;
import net.microfalx.bootstrap.model.Metadata;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * An abstract over a collection of records with CRUD support.
 */
@NoRepositoryBean
public interface DataSet<M, F extends Field<M>, ID> extends ListPagingAndSortingRepository<M, ID>, ListCrudRepository<M, ID> {

    /**
     * Returns the factory which made this data set instance.
     *
     * @return a non-null instance
     */
    DataSetFactory<M, F, ID> getFactory();

    /**
     * Returns the metadata for the model.
     *
     * @return a non-null instance
     */
    Metadata<M, F> getMetadata();

    /**
     * Returns whether the data set is read-only.
     *
     * @return {@code true} if the data set is read-only, {@code false} otherwise
     */
    boolean isReadOnly();

    /**
     * Returns the model identifier.
     *
     * @param model the model
     * @return the identifier
     */
    ID getId(M model);

    /**
     * Returns a {@link Filter} used to restrict
     *
     * @param pageable   the page information
     * @param filterable the filter information
     * @return a page of mode;s
     */
    Page<M> findAll(Pageable pageable, Filter filterable);
}
