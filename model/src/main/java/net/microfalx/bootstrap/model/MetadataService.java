package net.microfalx.bootstrap.model;

import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static net.microfalx.lang.AnnotationUtils.sort;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A service which provides metadata about (data) models.
 */
@Service
public class MetadataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataService.class);

    private final List<MetadataProvider<?, ?>> providers = new CopyOnWriteArrayList<>();
    private final Map<Class<?>, Metadata<?, ? extends Field<?>>> metadataCache = new ConcurrentHashMap<>();

    /**
     * Returns the metadata.
     *
     * @param modelClass the model class
     * @param <M>        the model type
     * @param <F>        the field type
     * @return a non-null instance
     */
    @SuppressWarnings("unchecked")
    public <M, F extends Field<M>> Metadata<M, F> getMetadata(Class<M> modelClass) {
        requireNonNull(modelClass);
        Metadata<M, F> metadata = (Metadata<M, F>) metadataCache.get(modelClass);
        if (metadata == null) {
            MetadataProvider<M, Field<M>> provider = find(modelClass);
            metadata = (Metadata<M, F>) provider.getMetadata(modelClass);
            metadataCache.put(modelClass, metadata);
        }
        return metadata;
    }

    /**
     * Returns all registered providers.
     *
     * @return a non-null instance
     */
    public Collection<MetadataProvider<?, ?>> getProviders() {
        return Collections.unmodifiableCollection(providers);
    }

    /**
     * Clears internal caches.
     */
    public void clear() {
        metadataCache.clear();
    }

    @PostConstruct
    protected void initialize() {
        discoverProviders();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <M, F extends Field<M>> MetadataProvider<M, F> find(Class modelClass) {
        for (MetadataProvider<?, ?> provider : providers) {
            if (provider.supports(modelClass)) return (MetadataProvider<M, F>) provider;
        }
        throw new ModelException("A metadata provider is not registered for model " + ClassUtils.getName(modelClass));
    }

    @SuppressWarnings("rawtypes")
    private void discoverProviders() {
        LOGGER.info("Discover data set factories:");
        ServiceLoader<MetadataProvider> scannedProviders = ServiceLoader.load(MetadataProvider.class);
        for (MetadataProvider<?, ?> scannedProvider : scannedProviders) {
            LOGGER.info(" - " + ClassUtils.getName(scannedProvider));
            providers.add(scannedProvider);
        }
        sort(providers);
    }
}
