package net.microfalx.bootstrap.dataset;

import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.lang.ArgumentUtils;
import net.microfalx.lang.ClassUtils;

import java.lang.reflect.Constructor;

import static net.microfalx.lang.ClassUtils.getClassParametrizedType;

/**
 * A factory created to support a data set for one class.
 *
 * @param <M>  the model type
 * @param <F>  the field type
 * @param <ID> the identifier type
 */
class ProviderDataSetFactory<M, F extends Field<M>, ID> extends AbstractDataSetFactory<M, F, ID> {

    private final Class<DataSet<M, F, ID>> dataSetClass;
    private final Class<M> modelClass;

    ProviderDataSetFactory(Class<DataSet<M, F, ID>> dataSetClass) {
        ArgumentUtils.requireNonNull(dataSetClass);
        this.dataSetClass = dataSetClass;
        this.modelClass = getClassParametrizedType(dataSetClass, 0);
    }

    @Override
    protected AbstractDataSet<M, F, ID> doCreate(Metadata<M, F> metadata) {
        try {
            Constructor<DataSet<M, F, ID>> constructor = dataSetClass.getDeclaredConstructor(DataSetFactory.class, Metadata.class);
            return (AbstractDataSet<M, F, ID>) constructor.newInstance(this, metadata);
        } catch (Exception e) {
            throw new DataSetException("Cannot create data set instance for " + ClassUtils.getName(dataSetClass), e);
        }
    }

    @Override
    public boolean supports(Metadata<M, F> metadata) {
        return metadata.getModel() == modelClass;
    }
}
