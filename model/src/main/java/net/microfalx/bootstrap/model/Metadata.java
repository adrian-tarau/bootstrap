package net.microfalx.bootstrap.model;

import net.microfalx.lang.Descriptable;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;
import net.microfalx.lang.annotation.Glue;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

/**
 * An interface which holds metadata about a model's fields.
 */
public interface Metadata<M, F extends Field<M>, ID> extends Identifiable<String>, Nameable, Descriptable {

    /**
     * Creates a metadata instance for a POJO.
     * <p>
     * Mostly used for testing or when handling the class as a normal POJO is enough.
     *
     * @param type the POJO type
     * @param <M>  the model type
     * @param <F>  the field type
     * @param <ID> the identifier type
     * @return the metadata
     */
    @SuppressWarnings("unchecked")
    static <M, F extends Field<M>, ID> Metadata<M, F, ID> create(Class<M> type) {
        PojoMetadataProvider.DefaultPojoMetadata<M, Object> metadata = new PojoMetadataProvider.DefaultPojoMetadata<>(type);
        metadata.initialize();
        return (Metadata<M, F, ID>) metadata;
    }

    /**
     * Creates a new instance of the model.
     *
     * @return a non-null instance
     */
    M create();

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
     * The annotation {@link net.microfalx.lang.annotation.Name} will be used to decide which field is used to create
     * tbe name of the field.
     * <p>
     * If there are multiple fields, they will be separated by spaces or separator provider by {@link Glue} annotation.
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
     * Returns all fields with a given annotation.
     *
     * @param annotationClass the data type
     * @return a non-null list
     */
    <A extends Annotation> List<F> getFields(Class<A> annotationClass);

    /**
     * Returns the field which identifies the record.
     *
     * @return the field, null if there is no identifier
     */
    F findIdField();

    /**
     * Returns the field which identifies the (main) timestamp.
     * <p>
     * Any field annotated with {@link net.microfalx.lang.annotation.Timestamp}, {@link net.microfalx.lang.annotation.CreatedAt}
     * or {@link net.microfalx.lang.annotation.ModifiedAt} will be considered, in this order.
     *
     * @return the field, null if there is no field that holds the timestamp
     * @see net.microfalx.lang.annotation.Timestamp
     * @see net.microfalx.lang.annotation.CreatedAt
     * @see net.microfalx.lang.annotation.ModifiedAt
     */
    F findTimestampField();

    /**
     * Returns the field which identifies the timestamp when the record was created.
     *
     * @return the field, null if there is no field that holds the timestamp
     * @see net.microfalx.lang.annotation.CreatedAt
     */
    F findCreatedAtField();

    /**
     * Returns the field which identifies the timestamp when the model was modified.
     *
     * @return the field, null if there is no field that holds the timestamp
     * @see net.microfalx.lang.annotation.ModifiedAt
     */
    F findModifiedAtField();

    /**
     * Returns the field which identifies the principal that created the model.
     * <p>
     * The value stored represents the username associated with the principal.
     *
     * @return the field, null if there is no field that holds the principal
     * @see net.microfalx.lang.annotation.CreatedBy
     */
    F findCreatedByField();

    /**
     * Returns the field which identifies the principal that modified the model.
     * <p>
     * The value stored represents the username associated with the principal.
     *
     * @return the field, null if there is no field that holds the principal
     * @see net.microfalx.lang.annotation.ModifiedBy
     */
    F findModifiedByField();

    /**
     * Returns a field by its name or property name, if exists.
     *
     * @param nameOrProperty the name or property name
     * @return the field, null if it does not exist
     */
    F find(String nameOrProperty);

    /**
     * Returns a field annotated with a given annotation, if exists.
     *
     * @param annotationClass the annotation class
     * @param <A>             the annotation type
     * @return the field, null if it does not exist
     */
    <A extends Annotation> F findAnnotated(Class<A> annotationClass);

    /**
     * Returns a field by its name or property name.
     *
     * @param nameOrProperty the name or property name
     * @return the field
     * @throws FieldNotFoundException if the field does not exist
     */
    F get(String nameOrProperty);

    /**
     * Returns a field annotated with a given annotation
     *
     * @param annotationClass the annotation class
     * @param <A>             the annotation type
     * @return the field
     * @throws FieldNotFoundException if the field does not exist
     */
    <A extends Annotation> F getAnnotated(Class<A> annotationClass);

    /**
     * Returns the model name.
     *
     * @param model the model
     * @return the name
     */
    String getName(M model);

    /**
     * Returns the name for a model.
     *
     * @param model            the model
     * @param includeSecondary {@code true} to include secondary name, {@code false} otherwise
     * @return the identifier
     */
    String getName(M model, boolean includeSecondary);

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
     * Returns whether two models have the same fields.
     * <p>
     * If both models are NULL, they are considered identical.
     *
     * @param firstModel  the first model
     * @param secondModel the second model
     * @return {@code true} if identical, {@code false} otherwise
     */
    boolean identical(M firstModel, M secondModel);

    /**
     * Creates a shallow copy of the model.
     *
     * @param model the model
     * @return a new instance with the same fields
     */
    M copy(M model);

    /**
     * Creates a shallow copy of the model.
     *
     * @param model the model
     * @param deep  {@code true} to make a deep copy,  {@code false} otherwise
     * @return a new instance, as a shallow or deep copy
     */
    M copy(M model, boolean deep);

    /**
     * Validates a model.
     *
     * @param model the model
     * @return the errors, empty if there are no errors
     */
    Map<F, String> validate(M model);

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
