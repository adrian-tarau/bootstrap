package net.microfalx.bootstrap.web.dataset;

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
    Metadata<M, F> getMetadata();

    /**
     * Returns the state of the data set.
     *
     * @return a non-null instance
     */
    State getState();

    /**
     * Opens the data set to edit a record.
     */
    void edit();

    /**
     * Opens the data set to append a record.
     */
    void append();

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
