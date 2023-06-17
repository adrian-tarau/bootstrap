package net.microfalx.bootstrap.web.dataset;

import net.microfalx.lang.Descriptable;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;

import java.util.Collection;

/**
 * An interface which holds metadata about a model's fields.
 */
public interface Metadata<M> extends Identifiable<String>, Nameable, Descriptable {

    /**
     * Returns the class supporting the model.
     *
     * @return a non-null instance
     */
    Class<M> getModel();

    /**
     * Returns the fields part of a record.
     *
     * @return a non-null instance
     */
    Collection<Field<?>> getFields();

    /**
     * Finds a field by its name or property name.
     *
     * @param nameOrProperty the name or property name
     * @return the field, null if it does not exist
     */
    Field<?> find(String nameOrProperty);

    /**
     * Gets a field by its name or property name.
     *
     * @param nameOrProperty the name or property name
     * @return the field, null if it does not exist
     * @throws DataSetException if the field does not exist
     */
    Field<?> get(String nameOrProperty);
}
