package net.microfalx.bootstrap.model;

/**
 * An interface which creates metadata.
 *
 * @param <M> the model type
 * @param <F> the field type
 */
public interface MetadataProvider<M, F extends Field<M>, ID> {

    /**
     * Returns whether the factory supports a given model class.
     *
     * @param modelClass the model class
     * @return {@code true} if it supports, {@code otherwise}
     */
    boolean supports(Class<M> modelClass);

    /**
     * Returns the metadata about the model.
     *
     * @param modelClass the model class
     * @return a non-null instance
     */
    Metadata<M, F, ID> getMetadata(Class<M> modelClass);
}
