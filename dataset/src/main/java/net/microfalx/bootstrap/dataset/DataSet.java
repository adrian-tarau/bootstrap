package net.microfalx.bootstrap.dataset;

import net.microfalx.bootstrap.model.CompositeIdentifier;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.model.Filter;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.lang.Nameable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

/**
 * An abstract over a collection of records with CRUD support.
 */
@NoRepositoryBean
public interface DataSet<M, F extends Field<M>, ID> extends Nameable, ListPagingAndSortingRepository<M, ID>, ListCrudRepository<M, ID> {

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
    Metadata<M, F, ID> getMetadata();

    /**
     * Returns the state of the data set.
     *
     * @return a non-null instance
     */
    State getState();

    /**
     * Changes the state of the data set.
     *
     * @param state the state
     * @return self
     */
    DataSet<M, F, ID> setState(State state);

    /**
     * Opens the data set to view a model.
     */
    DataSet<M, F, ID> view();

    /**
     * Opens the data set to edit a model.
     */
    DataSet<M, F, ID> edit();

    /**
     * Opens the data set to append a model.
     */
    DataSet<M, F, ID> add();

    /**
     * Returns the name of the data set (title).
     * <p>
     * By default, it returns the model name.
     *
     * @return a non-null instance
     */
    @Override
    String getName();

    /**
     * Changes the name of the data set.
     *
     * @param name the new name
     */
    void setName(String name);

    /**
     * Returns whether the data set is read-only.
     *
     * @return {@code true} if the data set is read-only, {@code false} otherwise
     */
    boolean isReadOnly();

    /**
     * Returns whether the field is read-only for this data set in the current mode.
     *
     * @param field the field
     * @return {@code true} if read-only, {@code false} otherwise
     */
    boolean isVisible(Field<M> field);

    /**
     * Returns a list with visible fields for the current state, sorted by position.
     *
     * @return a non-null instance
     * @see #getState()
     */
    List<Field<M>> getVisibleFields();

    /**
     * Returns the value of a field for a given model in a display format.
     *
     * @param model the model
     * @param field the field
     * @return the display value
     */
    String getDisplayValue(M model, Field<M> field);

    /**
     * Returns the model identifier.
     *
     * @param model the model
     * @return the identifier
     */
    CompositeIdentifier<M, F, ID> getCompositeId(M model);

    /**
     * Sets the composite identifier into model
     *
     * @param model the model
     * @param id    the composite identifier
     */
    void setCompositeId(M model, CompositeIdentifier<M, F, ID> id);

    /**
     * Returns the model identifier.
     *
     * @param model the model
     * @return the identifier
     */
    ID getId(M model);

    /**
     * Sets the ID into the model.
     *
     * @param model the model
     * @param id    the id
     */
    void setId(M model, ID id);

    /**
     * Validates whether the filter is valid for the data set.
     *
     * This validation includes whether the fields exists or if the values of the filter are acceptble for the data set.
     *
     * @param filter the filter
     * @throws DataSetException if the filter is not valid
     */
    void validate(Filter filter);

    /**
     * Returns a page of results based on page information and a filter.
     *
     * @param pageable   the page information
     * @param filterable the filter information
     * @return a page of mode;s
     */
    Page<M> findAll(Pageable pageable, Filter filterable);
}
