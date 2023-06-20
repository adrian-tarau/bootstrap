package net.microfalx.bootstrap.model;

import net.microfalx.bootstrap.core.config.I18nConfig;
import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
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
public class MetadataService implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataService.class);

    @Autowired
    private MessageSource messageSource;

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
            if (metadata instanceof AbstractMetadata<M, F> ametadata) {
                ametadata.messageSource = messageSource;
                ametadata.initialize();
            }
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

    @Override
    public void afterPropertiesSet() throws Exception {
        initialize();
    }

    protected void initialize() {
        initializeI18n();
        discoverProviders();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <M, F extends Field<M>> MetadataProvider<M, F> find(Class modelClass) {
        for (MetadataProvider<?, ?> provider : providers) {
            if (provider.supports(modelClass)) return (MetadataProvider<M, F>) provider;
        }
        throw new ModelException("A metadata provider is not registered for model " + ClassUtils.getName(modelClass));
    }

    private void initializeI18n() {
        if (messageSource == null) {
            messageSource = new I18nConfig().messageSource();
        }
    }

    @SuppressWarnings("rawtypes")
    private void discoverProviders() {
        LOGGER.info("Discover metadata providers:");
        ServiceLoader<MetadataProvider> scannedProviders = ServiceLoader.load(MetadataProvider.class);
        for (MetadataProvider<?, ?> scannedProvider : scannedProviders) {
            LOGGER.info(" - " + ClassUtils.getName(scannedProvider));
            providers.add(scannedProvider);
        }
        sort(providers);
    }
}
