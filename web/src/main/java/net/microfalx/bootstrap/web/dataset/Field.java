package net.microfalx.bootstrap.web.dataset;

import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;

/**
 * A field part of record in a data set.
 */
public interface Field<M> extends Identifiable<String>, Nameable {

    /**
     * Returns the metadata which owns this field.
     *
     * @return a non-null instance
     */
    Metadata<M> getMetadata();

    /**
     * Returns the name of the field.
     * <p>
     * The field name can be different from property name for some models (table column vs bean property for JPA entities).
     *
     * @return a non-null instance
     */
    String getName();

    /**
     * Returns the property name (class property).
     *
     * @return the property name
     */
    String getProperty();

    /**
     * Returns the index of the field within the model.
     *
     * @return a positive integer
     */
    int getIndex();

    /**
     * Returns whether the field is part of the model identifier.
     *
     * @return {@code true} if part of the model identifier, {@code false} otherwise
     */
    boolean isId();

    /**
     * Returns the class name supporting the field.
     *
     * @return a non-null instance
     */
    Class<?> getDataClass();

    /**
     * Returns an enum for the data type.
     *
     * @return a non-null instance
     */
    DataType getDataType();

    /**
     * Holds the data type of the field
     */
    enum DataType {

        /**
         * A boolean (yes/no)
         */
        BOOLEAN,

        /**
         * An integer (byte/short/int/long)
         */
        INTEGER,

        /**
         * A floating point (float/double)
         */
        NUMBER,

        /**
         * A date.
         */
        DATE,

        /**
         * A time.
         */
        TIME,

        /**
         * A date/time.
         */
        DATE_TIME,

        /**
         * An enum
         */
        ENUM,

        /**
         * Another model
         */
        MODEL

    }
}
