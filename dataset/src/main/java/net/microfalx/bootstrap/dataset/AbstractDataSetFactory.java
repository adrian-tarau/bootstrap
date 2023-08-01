package net.microfalx.bootstrap.dataset;

import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.model.Metadata;
import org.apache.commons.lang3.ClassUtils;
import org.joor.Reflect;

import java.util.Collection;

import static org.apache.commons.lang3.ClassUtils.isAssignable;

/**
 * Base class for all data set factories.
 *
 * @param <M> the model type
 */
public abstract class AbstractDataSetFactory<M, F extends Field<M>, ID> implements DataSetFactory<M, F, ID> {

    @Override
    public final DataSet<M, F, ID> create(Metadata<M, F> metadata, Object... parameters) {
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

    }

    /**
     * Subclasses would create the actual dataset.
     *
     * @param metadata the model metadata
     * @return a non0-null instance
     */
    protected abstract AbstractDataSet<M, F, ID> doCreate(Metadata<M, F> metadata);

    /**
     * Finds a parameter by type.
     *
     * @param type       the type
     * @param parameters the parameters
     * @param <T>        the data type
     * @return the parameter, null if does not exist
     */
    @SuppressWarnings("unchecked")
    protected final <T> T find(Class<T> type, Object... parameters) {
        for (Object parameter : parameters) {
            if (isAssignable(parameter.getClass(), type)) return (T) parameter;
            Collection<Reflect> fields = Reflect.on(parameter).fields().values();
            for (Reflect field : fields) {
                if (ClassUtils.isAssignable(field.type(), type)) return field.get();
            }
        }
        return null;
    }


}
