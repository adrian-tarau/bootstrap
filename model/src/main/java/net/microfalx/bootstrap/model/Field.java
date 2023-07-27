package net.microfalx.bootstrap.model;

import net.microfalx.lang.Descriptable;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;

import java.lang.annotation.Annotation;

/**
 * A field part of record in a data set.
 */
public interface Field<M> extends Identifiable<String>, Nameable, Descriptable {

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
     * Returns the label associated with the field.
     *
     * @return a non-null instance
     * @see net.microfalx.lang.annotation.I18n
     */
    String getLabel();

    /**
     * Returns the description associated with a field.
     * <p>
     * The description is usually displayed as a tooltip in UIs.
     *
     * @return the description, null if not defined
     */
    String getDescription();

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
     * Returns the position of the field for UI representation
     *
     * @return a positive integer
     * @see net.microfalx.lang.annotation.Position
     */
    int getPosition();

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
     * Returns the display value for a field value.
     *
     * @param model the model
     * @return the display value
     */
    String getDisplay(M model);

    /**
     * Holds the data type of the field
     */
    enum DataType {

        /**
         * A boolean (yes/no)
         */
        BOOLEAN(false, false),

        /**
         * An integer (byte/short/int/long)
         */
        INTEGER(true, false),

        /**
         * A floating point (float/double)
         */
        NUMBER(true, false),

        /**
         * Characters
         */
        STRING(false, false),

        /**
         * A date.
         */
        DATE(false, true),

        /**
         * A time.
         */
        TIME(false, true),

        /**
         * A date/time.
         */
        DATE_TIME(false, true),

        /**
         * An enum
         */
        ENUM(false, false),

        /**
         * A collection class
         */
        COLLECTION(false, false),

        /**
         * Another model
         */
        MODEL(false, false);

        private boolean numeric;
        private boolean temporal;

        DataType(boolean numeric, boolean temporal) {
            this.numeric = numeric;
            this.temporal = temporal;
        }

        public boolean isNumeric() {
            return numeric;
        }

        public boolean isTemporal() {
            return temporal;
        }
    }
}
