package net.microfalx.bootstrap.model;

import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;

import java.lang.annotation.Annotation;

/**
 * A field part of record in a data set.
 */
public interface Field<M> extends Identifiable<String>, Nameable {

    /**
     * Returns the metadata which owns this field.
     *
     * @return a non-null instance
     */
    Metadata<M, ? extends Field<M>> getMetadata();

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
     * Returns whether the field is read-only.
     *
     * @return {@code true} if read-only, {@code false} otherwise
     */
    boolean isReadOnly();

    /**
     * Returns whether the field is transient (not persisted).
     *
     * @return {@code true} if transient, {@code false} otherwise
     */
    boolean isTransient();

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
     * Returns an annotation by its type.
     *
     * @param annotationClass the annotation type
     * @param <A>             the annotation type
     * @return the annotation, null if not registered
     */
    <A extends Annotation> A findAnnotation(Class<A> annotationClass);

    /**
     * Returns whether an annotation with a given type exists.
     *
     * @param annotationClass the annotation type
     * @param <A>             the annotation type
     * @return the annotation, null if not registered
     */
    <A extends Annotation> boolean hasAnnotation(Class<A> annotationClass);

    /**
     * Returns the value from the model for a given field.
     *
     * @param model the model
     * @return the value
     */
    Object get(M model);

    /**
     * Changes the value in the model for a given field.
     *
     * @param model the model
     * @param value the value
     */
    void set(M model, Object value);

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
         * Characters
         */
        STRING,

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
         * A collection class
         */
        COLLECTION,

        /**
         * Another model
         */
        MODEL

    }
}