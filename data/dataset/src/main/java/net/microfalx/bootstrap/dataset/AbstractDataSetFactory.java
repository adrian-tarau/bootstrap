package net.microfalx.bootstrap.dataset;

import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.model.Metadata;

/**
 * Base class for all data set factories.
 *
 * @param <M> the model type
 */
public abstract class AbstractDataSetFactory<M, F extends Field<M>, ID> implements DataSetFactory<M, F, ID> {

    DataSetService dataSetService;

    @Override
    public final DataSet<M, F, ID> create(Metadata<M, F, ID> metadata, Object... parameters) {
        AbstractDataSet<M, F, ID> dataSet = doCreate(metadata);
        update(dataSet, parameters);
        return dataSet;
    }

    /**
     * Updates the data set with information.
     *
     * @param dataSet    the data set
     * @param parameters the parameters
     */
    void update(AbstractDataSet<M, F, ID> dataSet, Object... parameters) {
        // empty by default, subclasses might perform some actions
    }

    /**
     * Subclasses would create the actual dataset.
     *
     * @param metadata the model metadata
     * @return a non0-null instance
     */
    protected abstract AbstractDataSet<M, F, ID> doCreate(Metadata<M, F, ID> metadata);

    /**
     * Finds a parameter by type.
     *
     * @param type       the type
     * @param parameters the parameters
     * @param <T>        the data type
     * @return the parameter, null if does not exist
     */
    protected final <T> T find(Class<T> type, Object... parameters) {
        return DataSetUtils.find(type, parameters);
    }
}
