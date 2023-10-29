package net.microfalx.bootstrap.model;

import net.microfalx.bootstrap.core.i18n.I18nConfig;
import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.validation.Validator;

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

    @Autowired
    private Validator validator;

    private final List<MetadataProvider<?, ?, ?>> providers = new CopyOnWriteArrayList<>();
    private final Map<Class<?>, Metadata<?, ? extends Field<?>, ?>> metadataCache = new ConcurrentHashMap<>();

    /**
     * Returns the metadata.
     *
     * @param model the model
     * @param <M>   the model type
     * @param <F>   the field type
     * @return a non-null instance
     */
    @SuppressWarnings("unchecked")
    public <M, F extends Field<M>, ID> Metadata<M, F, ID> getMetadata(Object model) {
        requireNonNull(model);
        return getMetadata((Class<M>) model.getClass());
    }

    /**
     * Returns the metadata.
     *
     * @param modelClass the model class
     * @param <M>        the model type
     * @param <F>        the field type
     * @return a non-null instance
     */
    @SuppressWarnings("unchecked")
    public <M, F extends Field<M>, ID> Metadata<M, F, ID> getMetadata(Class<M> modelClass) {
        requireNonNull(modelClass);
        Metadata<M, F, ID> metadata = (Metadata<M, F, ID>) metadataCache.get(modelClass);
        if (metadata == null) {
            MetadataProvider<M, Field<M>, ID> provider = find(modelClass);
            metadata = (Metadata<M, F, ID>) provider.getMetadata(modelClass);
            if (metadata instanceof AbstractMetadata<M, F, ID> ametadata) {
                ametadata.messageSource = messageSource;
                ametadata.validator = validator;
                ametadata.metadataService = this;
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
    public Collection<MetadataProvider<?, ?, ?>> getProviders() {
        return Collections.unmodifiableCollection(providers);
    }

    /**
     * Clears internal caches.
     */
    public void clear() {
        metadataCache.clear();
    }

    /**
     * Validates a model.
     *
     * @param model the model
     * @return the errors, empty if there are no errors
     */
    @SuppressWarnings("unchecked")
    public <M, F extends Field<M>, ID> Map<Field<M>, String> validate(M model) {
        requireNonNull(model);
        Metadata<M, F, ID> metadata = getMetadata(model);
        return (Map<Field<M>, String>) metadata.validate(model);
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
    private <M, F extends Field<M>, ID> MetadataProvider<M, F, ID> find(Class modelClass) {
        for (MetadataProvider<?, ?, ?> provider : providers) {
            if (provider.supports(modelClass)) return (MetadataProvider<M, F, ID>) provider;
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
        for (MetadataProvider<?, ?, ?> scannedProvider : scannedProviders) {
            LOGGER.info(" - " + ClassUtils.getName(scannedProvider));
            providers.add(scannedProvider);
        }
        sort(providers);
    }
}
