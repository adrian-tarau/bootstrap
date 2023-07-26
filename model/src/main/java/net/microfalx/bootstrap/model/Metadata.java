package net.microfalx.bootstrap.model;

import net.microfalx.lang.Descriptable;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * An interface which holds metadata about a model's fields.
 */
public interface Metadata<M, F extends Field<M>> extends Identifiable<String>, Nameable, Descriptable {

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
     * Returns the field which identifies the record.
     * @return the field, null if there is no identifier
     */
    F findIdField();

    /**
     * Finds a field by its name or property name.
     *
     * @param nameOrProperty the name or property name
     * @return the field, null if it does not exist
     */
    F find(String nameOrProperty);

    /**
     * Gets a field by its name or property name.
     *
     * @param nameOrProperty the name or property name
     * @return the field, null if it does not exist
     * @throws FieldNotFoundException if the field does not exist
     */
    Field<?> get(String nameOrProperty);

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
