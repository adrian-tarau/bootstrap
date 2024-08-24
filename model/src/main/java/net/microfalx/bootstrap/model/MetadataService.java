package net.microfalx.bootstrap.model;

import net.microfalx.bootstrap.core.i18n.I18nService;
import net.microfalx.lang.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Validator;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static net.microfalx.lang.AnnotationUtils.sort;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ClassUtils.isSubClassOf;

/**
 * A service which provides metadata about (data) models.
 */
@Service
public class MetadataService implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataService.class);

    @Autowired
    private Validator validator;

    @Autowired
    private I18nService i18nService;

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

    /**
     * Copies all the fields with the same name and type from the source model to the target model.
     *
     * @param source the source
     * @param target the target
     * @param <MS>   the model type of the source
     * @param <FS>   the field type of the source model
     * @param <IDS>  the identifier type of the source model
     * @param <MT>   the model type of the source
     * @param <FT>   the field type of the source model
     * @param <IDT>  the identifier type of the source model
     * @return the target
     */
    public <MS, FS extends Field<MS>, IDS, MT, FT extends Field<MT>, IDT> MT copy(MS source, MT target) {
        requireNonNull(source);
        requireNonNull(target);
        Metadata<MS, FS, IDS> sourceMetadata = getMetadata(source);
        Metadata<MT, FT, IDT> targetMetadata = getMetadata(target);
        for (FS sourceField : sourceMetadata.getFields()) {
            FT targetField = targetMetadata.find(sourceField.getName());
            if (targetField != null && isSubClassOf(targetField.getDataClass(), sourceField.getDataClass())) {
                targetField.set(target, sourceField.get(source));
            }
        }
        return target;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initialize();
    }

    protected I18nService getI18nService() {
        return i18nService;
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
