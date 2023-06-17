package net.microfalx.bootstrap.web.dataset;

import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public final class DataSetService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSetService.class);

    private final Collection<DataSetFactory<?, ?>> factories = new CopyOnWriteArrayList<>();
    private final Map<Class<?>, Map<String, Field<?>>> fieldsCache = new ConcurrentHashMap<>();

    /**
     * Returns a data set from a model class.
     *
     * @param modelClass the model class
     * @param <M>        the model type
     * @return the data set
     * @throws DataSetException if a data set cannot be created
     */
    @SuppressWarnings("unchecked")
    public <M, ID> DataSet<M, ID> lookup(Class<M> modelClass) {
        requireNonNull(modelClass);
        for (DataSetFactory factory : factories) {
            if (factory.supports(modelClass)) return factory.create(modelClass);
        }
        throw new DataSetException("A data set cannot be created for model " + ClassUtils.getName(modelClass));
    }

    /**
     * Returns registered factories.
     *
     * @return a non-null instance.
     */
    public Collection<DataSetFactory<?, ?>> getFactories() {
        return unmodifiableCollection(factories);
    }

    @PostConstruct
    protected void initialize() {
        discoverFactories();
    }

    @SuppressWarnings("rawtypes")
    private void discoverFactories() {
        LOGGER.info("Discover data set factories:");
        ServiceLoader<DataSetFactory> scannedFactories = ServiceLoader.load(DataSetFactory.class);
        for (DataSetFactory<?, ?> scannedFactory : scannedFactories) {
            LOGGER.info(" - " + ClassUtils.getName(scannedFactory));
            factories.add(scannedFactory);
        }
    }
}
