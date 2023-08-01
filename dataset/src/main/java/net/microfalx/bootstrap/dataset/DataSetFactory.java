package net.microfalx.bootstrap.dataset;

import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.model.Metadata;

/**
 * A data set factory.
 */
public interface DataSetFactory<M, F extends Field<M>, ID> {

    /**
     * Returns whether the factory supports a given model class.
     *
     * @param metadata the metadata of the  model
     * @return {@code true} if it supports, {@code otherwise}
     */
    boolean supports(Metadata<M, F> metadata);

    /**
     * Creates a data set  supporting a given model class
     *
     * @param metadata the metadata of the  model
     * @return a non-null instance
     */
    DataSet<M, F, ID> create(Metadata<M, F> metadata, Object... parameters);
}
