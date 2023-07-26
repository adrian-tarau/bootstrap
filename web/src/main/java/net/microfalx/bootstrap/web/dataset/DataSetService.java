package net.microfalx.bootstrap.web.dataset;

import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.model.MetadataService;
import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.Collections.unmodifiableCollection;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A service used to create data sets
 */
@Service
public final class DataSetService implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSetService.class);

    private final Collection<DataSetFactory<?, ?, ?>> factories = new CopyOnWriteArrayList<>();
    private final Map<Class<?>, Map<String, Field<?>>> fieldsCache = new ConcurrentHashMap<>();

    @Autowired
    private MetadataService metadataService;

    @Autowired
    ApplicationContext applicationContext;

    /**
     * Returns a data set from a model class.
     *
     * @param modelClass the model class
     * @param <M>        the model type
     * @return the data set
     * @throws DataSetException if a data set cannot be created
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <M, F extends Field<M>, ID> DataSet<M, F, ID> lookup(Class<M> modelClass, Object... parameters) {
        requireNonNull(modelClass);
        Metadata<M, Field<M>> metadata = metadataService.getMetadata(modelClass);
        DataSet<M, F, ID> dataSet = null;
        for (DataSetFactory factory : factories) {
            if (factory.supports(metadata)) {
                dataSet = factory.create(metadata, parameters);
                break;
            }
        }
        if (dataSet != null) {
            ((AbstractDataSet) dataSet).applicationContext = applicationContext;
            return dataSet;
        } else {
            throw new DataSetException("A data set cannot be created for model " + ClassUtils.getName(modelClass));
        }
    }

    /**
     * Returns registered factories.
     *
     * @return a non-null instance.
     */
    public Collection<DataSetFactory<?, ?, ?>> getFactories() {
        return unmodifiableCollection(factories);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initialize();
    }

    protected void initialize() {
        discoverFactories();
    }

    @SuppressWarnings("rawtypes")
    private void discoverFactories() {
        LOGGER.info("Discover data set factories:");
        ServiceLoader<DataSetFactory> scannedFactories = ServiceLoader.load(DataSetFactory.class);
        for (DataSetFactory<?, ?, ?> scannedFactory : scannedFactories) {
            LOGGER.info(" - " + ClassUtils.getName(scannedFactory));
            factories.add(scannedFactory);
        }
    }
}
