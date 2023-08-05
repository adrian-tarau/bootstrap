package net.microfalx.bootstrap.dataset;

import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.model.MetadataService;
import net.microfalx.lang.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.lang.ref.SoftReference;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.Collections.unmodifiableCollection;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.FormatterUtils.formatDateTime;

/**
 * A service used to create data sets
 */
@Service
public final class DataSetService implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSetService.class);

    private final Collection<DataSetFactory<?, ?, ?>> factories = new CopyOnWriteArrayList<>();
    private final Map<Class<?>, SoftReference<CachedModels<?, ?>>> caches = new ConcurrentHashMap<>();

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
        Metadata<M, Field<M>, ID> metadata = metadataService.getMetadata(modelClass);
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

    /**
     * Registers a list of models into the cache.
     *
     * @param models the models
     * @param <M>    the model type
     */
    <M, ID> void registerCache(CachedModels<M, ID> models) {
        requireNonNull(models);
        caches.put(models.modelClass, new SoftReference<>(models));
    }

    /**
     * Returns a list of cached models from the cache.
     *
     * @param modelClass the model class
     * @param <M>        the model type
     * @return the cached models, null if there is nothing in the cache
     */
    @SuppressWarnings("unchecked")
    <M, ID> CachedModels<M, ID> getCached(Class<M> modelClass) {
        requireNonNull(modelClass);
        SoftReference<CachedModels<?, ?>> reference = caches.get(modelClass);
        CachedModels<M, ID> holder = reference != null ? (CachedModels<M, ID>) reference.get() : null;
        return holder != null && !holder.isExpired() ? holder : null;
    }

    protected void initialize() {
        discoverStaticFactories();
        discoverDynamicFactories();
    }

    @SuppressWarnings("rawtypes")
    private void discoverStaticFactories() {
        LOGGER.info("Discover static data set factories:");
        ServiceLoader<DataSetFactory> scannedFactories = ServiceLoader.load(DataSetFactory.class);
        for (DataSetFactory<?, ?, ?> scannedFactory : scannedFactories) {
            LOGGER.info(" - " + ClassUtils.getName(scannedFactory));
            factories.add(scannedFactory);
        }
    }

    private void discoverDynamicFactories() {
        LOGGER.info("Discover dynamic data set factories:");
        Collection<Class<DataSet>> dataSetClasses = ClassUtils.resolveProviders(DataSet.class);
        for (Class<DataSet> dataSetClass : dataSetClasses) {
            LOGGER.info(" - " + ClassUtils.getName(dataSetClass));
            factories.add(new ProviderDataSetFactory(dataSetClass));
        }
    }

    static class CachedModels<M, ID> {

        private final Class<M> modelClass;
        private final List<M> models;
        private final Map<ID, M> modelsById;
        private final Duration expiration;
        private final long created = System.currentTimeMillis();

        CachedModels(Class<M> modelClass, List<M> models, Map<ID, M> modelsById, Duration expiration) {
            this.modelClass = modelClass;
            this.models = models;
            this.modelsById = modelsById;
            this.expiration = expiration;
        }

        List<M> getModels() {
            return models;
        }

        Map<ID, M> getModelsById() {
            return modelsById;
        }

        private boolean isExpired() {
            return (System.currentTimeMillis() - created) > expiration.toMillis();
        }

        @Override
        public String toString() {
            return "CacheHolder{" +
                    "models=" + models.size() +
                    ", expiration=" + expiration +
                    ", created=" + formatDateTime(created) +
                    '}';
        }
    }
}
