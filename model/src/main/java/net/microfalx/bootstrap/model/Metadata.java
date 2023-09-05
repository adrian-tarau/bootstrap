package net.microfalx.bootstrap.model;

import net.microfalx.lang.Descriptable;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * An interface which holds metadata about a model's fields.
 */
public interface Metadata<M, F extends Field<M>, ID> extends Identifiable<String>, Nameable, Descriptable {

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
    List<F> getFields();

    /**
     * Returns the fields part of the record identifier.
     *
     * @return a non-null instance
     */
    List<F> getIdFields();

    /**
     * Returns the fields which gives name to a record.
     * <p>
     * The annotation {@link net.microfalx.lang.annotation.Name} will be used to decide which field i
     * <p>
     * If there are multiple fields, they will be separated by spaces.
     *
     * @return the fields
     * @throws FieldNotFoundException if at least one fi
     */
    List<F> getNameFields();

    /**
     * Returns all fields with a given data type.
     *
     * @param dataType the data type
     * @return a non-null list
     */
    List<F> getFields(Field.DataType dataType);

    /**
     * Returns the field which identifies the record.
     *
     * @return the field, null if there is no identifier
     */
    F findIdField();

    /**
     * Returns the field which identifies the (main) timestamp.
     *
     * @return the field, null if there is no field which holds the timestamp
     */
    F findTimestampField();

    /**
     * Returns a field by its name or property name, if exists.
     *
     * @param nameOrProperty the name or property name
     * @return the field, null if it does not exist
     */
    F find(String nameOrProperty);

    /**
     * Returns a field by its name or property name.
     *
     * @param nameOrProperty the name or property name
     * @return the field
     * @throws FieldNotFoundException if the field does not exist
     */
    F get(String nameOrProperty);

    /**
     * Returns the model name.
     *
     * @param model the model
     * @return the name
     */
    String getName(M model);

    /**
     * Returns the class representing the model identifier.
     *
     * @return a non-null instance
     */
    Class<ID> getIdClass();

    /**
     * Returns the composite identifier for a given model.
     *
     * @param model the model
     * @return a non-null instance
     */
    CompositeIdentifier<M, F, ID> getId(M model);

    /**
     * Returns the composite identifier from its string representation.
     *
     * @param id the identifier
     * @return a non-nul instance
     */
    CompositeIdentifier<M, F, ID> getId(String id);

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
}
