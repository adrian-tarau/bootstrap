package net.microfalx.bootstrap.web.dataset;

/**
 * A data set factory.
 */
public interface DataSetFactory<M, ID> {

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
    Metadata<M> getMetadata(Class<M> modelClass);

    /**
     * Parses the expression and creates
     * @param value
     * @return
     */
    Expression parse(String value);

    /**
     * Updates the data set with information from the controller.
     *
     * @param dataSet the data set
     * @param owner   the owner (usually a controller
     */
    void update(DataSet<M, ID> dataSet, Object owner);

    /**
     * Creates a data set instance
     *
     * @param modelClass the data set model
     * @return a non-null instance
     */
    DataSet<M, ID> create(Class<M> modelClass);
}
