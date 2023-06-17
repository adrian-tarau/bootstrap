package net.microfalx.bootstrap.web.dataset;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * An abstract over a collection of records with CRUD support.
 */
@NoRepositoryBean
public interface DataSet<M, ID> extends ListPagingAndSortingRepository<M, ID>, ListCrudRepository<M, ID> {

    /**
     * Returns the factory which made this data set instance.
     *
     * @return a non-null instance
     */
    DataSetFactory<M, ID> getFactory();

    /**
     * Returns the metadata for the model.
     *
     * @return a non-null instance
     */
    Metadata<M> getMetadata();

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
     * Returns a {@link Page} of models meeting the paging restriction provided in the {@link Pageable} object and
     * record restrictions provided in the {@link Expression} object.
     *
     * @param pageable the pageable to request a paged result, can be {@link Pageable#unpaged()}, must not be
     *                 {@literal null}.
     * @return a page of entities
     */
    Page<M> findAll(Pageable pageable, Expression expression);
}
