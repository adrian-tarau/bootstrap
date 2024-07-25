package net.microfalx.bootstrap.dataset;

import net.microfalx.bootstrap.metrics.Matrix;
import net.microfalx.bootstrap.model.CompositeIdentifier;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.model.Filter;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.*;

/**
 * An abstract over a collection of records with CRUD support.
 */
@NoRepositoryBean
public interface DataSet<M, F extends Field<M>, ID> extends Identifiable<String>, Nameable,
        ListPagingAndSortingRepository<M, ID>, ListCrudRepository<M, ID> {

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
     * Returns whether the field is visible for this data set in the current mode.
     *
     * @param field the field
     * @return {@code true} if read-only, {@code false} otherwise
     */
    boolean isVisible(Field<M> field);

    /**
     * Returns whether the field is read-only for this data set in the current mode.
     *
     * @param field the field
     * @return {@code true} if read-only, {@code false} otherwise
     */
    boolean isReadOnly(Field<M> field);

    /**
     * Returns whether the field should be included in the search by value when searching for plain text in the
     * search field.
     *
     * @param field the field
     * @return {@code true} if read-only, {@code false} otherwise
     */
    boolean isSearchable(Field<M> field);

    /**
     * Returns whether the field should be used to add criteria to the filter by clicking on a field value
     * in UI.
     *
     * @param field the field
     * @return {@code true} if read-only, {@code false} otherwise
     */
    boolean isFilterable(Field<M> field);

    /**
     * Returns a list with visible fields for the current state, sorted by position.
     *
     * @return a non-null instance
     * @see #getState()
     */
    List<F> getVisibleFields();

    /**
     * Returns the value of a field for a given model in a display format.
     * <p>
     * If the field is supported by a model, the display value is the model name
     *
     * @param model the model
     * @param field the field
     * @return the display value
     * @see Metadata#getName(Object)
     */
    String getDisplayValue(M model, Field<M> field);

    /**
     * Returns the value behind a value or display value for a field value.
     *
     * @param displayValue the display value
     * @param field        the field
     * @param <T>          the type of value
     * @return the value
     */
    <T> T getValue(String displayValue, Field<M> field);

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
     * Returns the name for a model.
     *
     * @param model the model
     * @return the identifier
     */
    String getName(M model);

    /**
     * Validates a model.
     *
     * @param model the model
     * @return the errors, empty if there are no errors
     */
    Map<F, String> validate(M model);

    /**
     * Prepares a model for offline access.
     * <p>
     * Some data set requires additional context (for example an open session for JPAs) to detach the model.
     *
     * @param model the model
     */
    void detach(M model);

    /**
     * Validates whether the filter is valid for the data set.
     * <p>
     * This validation includes whether the fields exist or if the values of the filter are acceptable for the data set.
     *
     * @param filter the filter
     * @throws DataSetException if the filter is not valid
     */
    void validate(Filter filter);

    /**
     * Find first model with a given display name.
     *
     * @param displayValue the display value
     * @return a non-null instance
     */
    Optional<M> findByDisplayValue(String displayValue);

    /**
     * Returns a page of results based on page information and a filter.
     *
     * @param pageable   the page information
     * @param filterable the filter information
     * @return a page of models
     */
    Page<M> findAll(Pageable pageable, Filter filterable);

    /**
     * Returns the trend (distribution in time) of models.
     *
     * @param filterable the filter
     * @return matrix
     */
    Matrix getTrend(Filter filterable, int points);

    /**
     * Returns the trend (distribution in time) of fields.
     *
     * @param filterable the filter
     * @param fields     the fields/attributes
     * @return matrix
     */
    Collection<Matrix> getTrend(Filter filterable, Set<String> fields, int points);

    /**
     * Returns a set of fields which can be trended.
     *
     * @return a non-null instance
     */
    Set<String> getTrendFields();

    /**
     * Returns the estimate number of unique terms for a field.
     *
     * @param fieldName the field name
     * @return the estimated count, with negative sign if the estimation is incomplete
     */
    int getTrendTermCount(String fieldName);
}
