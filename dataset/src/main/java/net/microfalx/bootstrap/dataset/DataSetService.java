package net.microfalx.bootstrap.dataset;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.microfalx.bootstrap.core.i18n.I18nService;
import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.model.Filter;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.model.MetadataService;
import net.microfalx.lang.ClassUtils;
import net.microfalx.lang.Hashing;
import net.microfalx.lang.ObjectUtils;
import net.microfalx.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.support.Repositories;
import org.springframework.stereotype.Service;

import java.lang.ref.SoftReference;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.Collections.unmodifiableCollection;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ExceptionUtils.getRootCauseName;
import static net.microfalx.lang.FormatterUtils.formatDateTime;

/**
 * A service used to create data sets
 */
@Service
public final class DataSetService extends ApplicationContextSupport implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSetService.class);

    private final Collection<DataSetFactory<?, ?, ?>> factories = new CopyOnWriteArrayList<>();
    private final Map<Class<?>, DataSetFactory<?, ?, ?>> factoriesCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, LookupProvider<?, ?>> lookupProviders = new ConcurrentHashMap<>();
    private final Map<String, SoftReference<CachedModelsById<?, ?>>> cachesById = new ConcurrentHashMap<>();
    private final Map<Class<?>, SoftReference<CachedModelsByDisplayValue<?>>> cachesByDisplayName = new ConcurrentHashMap<>();
    private final Map<Class<?>, Repository<?, ?>> repositories = new ConcurrentHashMap<>();

    @Autowired
    private MetadataService metadataService;

    @Autowired
    private I18nService i18nService;

    /**
     * Returns the metadata associated with a model.
     *
     * @return a non-null instance
     */
    public <M, F extends Field<M>, ID> Metadata<M, F, ID> getMetadata(Class<M> modelClass) {
        return metadataService.getMetadata(modelClass);
    }

    /**
     * Returns the model identifier.
     *
     * @param model the model
     * @return the identifier
     */
    @SuppressWarnings("unchecked")
    public <M, ID> ID getId(M model) {
        requireNonNull(model);
        if (model instanceof Enum<?>) {
            return (ID) ((Enum<?>) model).name();
        } else if (ClassUtils.isBaseClass(model)) {
            return (ID) model;
        } else {
            DataSet<M, Field<M>, ID> dataSet = getDataSet((Class<M>) model.getClass());
            return dataSet.getId(model);
        }
    }

    /**
     * Returns the model name.
     *
     * @param model the model
     * @return the name
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <M> String getName(M model) {
        requireNonNull(model);
        if (model instanceof Enum<?>) {
            return DataSetUtils.getDisplayValue(i18nService, (Enum) model);
        } else {
            DataSet<M, Field<M>, ?> dataSet = getDataSet((Class<M>) model.getClass());
            return dataSet.getName(model);
        }
    }

    /**
     * Resolves the value of a field based on the value or display value of a field.
     * <p>
     * If the field is of type {@link Field.DataType#MODEL} or the field has a {@link Lookup} or
     * a {@link net.microfalx.bootstrap.dataset.formatter.Formatter} the method delegates the location to.
     *
     * @param field the field
     * @param value the value or the display value
     * @param <M>   the model
     * @return the model, null if such a model cannot be located
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public <M> Object resolve(Field<M> field, Object value) {
        requireNonNull(field);
        if (ObjectUtils.isEmpty(value)) return null;
        Class<M> modelClass = field.getMetadata().getModel();
        Class<?> valueClass = value.getClass();
        if (valueClass == field.getDataClass()) return value;
        String displayValue = value.toString();
        if (field.getDataType().isBoolean()) {
            return Field.from(value, Boolean.class);
        } else if (field.getDataType() == Field.DataType.STRING) {
            return Field.from(value, String.class);
        } else if (field.getDataType() == Field.DataType.INTEGER) {
            return Field.from(value, Long.class);
        } else if (field.getDataType() == Field.DataType.NUMBER) {
            return Field.from(value, Double.class);
        } else if (field.getDataType() == Field.DataType.MODEL) {
            CachedModelsByDisplayValue<?> cache = getCacheByDisplayName(valueClass);
            value = cache.models.getIfPresent(displayValue.toLowerCase());
            if (value != null) return value;
            DataSet fieldDataSet = getDataSet(field.getDataClass());
            Optional<Object> result = fieldDataSet.findByDisplayValue(displayValue);
            value = result.orElse(null);
            if (value != null) registerByDisplayName(value, displayValue);
        } else {
            DataSet<M, ? extends Field<M>, Object> dataSet = getDataSet(modelClass);
            value = dataSet.getValue(displayValue, field);
        }
        return value;
    }

    /**
     * Returns a data set for lookups from a model class.
     *
     * @param modelClass the model class
     * @param <M>        the model type
     * @return the data set
     * @throws DataSetException if a data set cannot be created
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <M, ID, L extends Lookup<ID>> LookupProvider<L, ID> getLookupProvider(Class<M> modelClass, Object... parameters) {
        requireNonNull(modelClass);
        LookupProvider<L, ID> lookupProvider;
        if (modelClass.isEnum()) {
            Class<? extends Enum> enumClass = (Class<? extends Enum>) modelClass;
            lookupProvider = new EnumLookupProvider<>(enumClass);
        } else {
            lookupProvider = (LookupProvider<L, ID>) lookupProviders.get(modelClass);
            if (lookupProvider == null) {
                DataSet<M, Field<M>, ?> dataSet = getDataSet(modelClass, parameters);
                lookupProvider = new ModelLookupProvider(DefaultLookup.class, dataSet);
            }
        }
        if (lookupProvider instanceof AbstractLookupProvider<L, ID> abstractLookupProvider) {
            abstractLookupProvider.dataSetService = this;
        }
        return lookupProvider;
    }

    /**
     * Returns a data set from a model class.
     *
     * @param modelClass the model class
     * @param <M>        the model type
     * @return the data set
     * @throws DataSetException if a data set cannot be created
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <M, F extends Field<M>, ID> DataSet<M, F, ID> getDataSet(Class<M> modelClass, Object... parameters) {
        requireNonNull(modelClass);
        if (ClassUtils.isBaseClass(modelClass)) {
            throw new DataSetException("Cannot provide a data set for a base JDK class (" + ClassUtils.getName(modelClass) + ")");
        }
        Metadata<M, Field<M>, ID> metadata = metadataService.getMetadata(modelClass);
        DataSetFactory cachedFactory = factoriesCache.get(modelClass);
        if (cachedFactory == null) {
            for (DataSetFactory factory : factories) {
                if (factory.supports(metadata)) {
                    cachedFactory = factory;
                    break;
                }
            }
        }
        if (cachedFactory != null) {
            DataSet<M, F, ID> dataSet = cachedFactory.create(metadata, parameters);
            AbstractDataSet abstractDataSet = (AbstractDataSet) dataSet;
            abstractDataSet.dataSetService = this;
            try {
                abstractDataSet.afterPropertiesSet();
            } catch (Exception e) {
                throw new DataSetException("A data set for model '" + ClassUtils.getName(modelClass) + "' failed to be initialized", e);
            }
            return dataSet;
        } else {
            throw new DataSetException("A data set cannot be created for model " + ClassUtils.getName(modelClass));
        }
    }

    /**
     * Returns the tooltip associated with the field.
     *
     * @param model the model
     * @param field the field
     * @param <M>   the model type
     * @param <F>   the field type
     * @return the alert, null
     */
    public <M, F extends Field<M>> Optional<String> getTooltip(M model, F field) {
        requireNonNull(model);
        requireNonNull(field);
        Formattable formattableAnnot = field.findAnnotation(Formattable.class);
        if (formattableAnnot == null || formattableAnnot.tooltip() == Formattable.TooltipProvider.class) {
            return Optional.empty();
        }
        Object value = field.get(model);
        Formattable.TooltipProvider<M, F, Object> tooltipProvider = ClassUtils.create(formattableAnnot.tooltip());
        if (tooltipProvider instanceof ApplicationContextAware) {
            ((ApplicationContextAware) tooltipProvider).setApplicationContext(getApplicationContext());
        }
        try {
            return Optional.of(tooltipProvider.provide(value, field, model));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Returns the alert associated with the field.
     *
     * @param model the model
     * @param field the field
     * @param <M>   the model type
     * @param <F>   the field type
     * @return the alert, null
     */
    public <M, F extends Field<M>> Optional<Alert> getAlert(M model, F field) {
        requireNonNull(model);
        requireNonNull(field);
        Formattable formattableAnnot = field.findAnnotation(Formattable.class);
        if (formattableAnnot == null || formattableAnnot.alert() == Formattable.AlertProvider.class) {
            return Optional.empty();
        }
        Object value = field.get(model);
        Formattable.AlertProvider<M, F, Object> alertProvider = ClassUtils.create(formattableAnnot.alert());
        if (alertProvider instanceof ApplicationContextAware) {
            ((ApplicationContextAware) alertProvider).setApplicationContext(getApplicationContext());
        }
        try {
            return Optional.of(alertProvider.provide(value, field, model));
        } catch (Exception e) {
            return Optional.of(Alert.builder().type(Alert.Type.DARK).message("#ERROR: " + getRootCauseName(e)).build());
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
    <M, ID> void registerCache(CachedModelsById<M, ID> models, Filter filterable) {
        requireNonNull(models);
        String cacheKey = getCacheKey(models.modelClass, filterable);
        cachesById.put(cacheKey, new SoftReference<>(models));
    }

    /**
     * Returns a cache with models by id.
     *
     * @param modelClass the model class
     * @param <M>        the model type
     * @return the cached models, null if there is nothing in the cache
     */
    @SuppressWarnings("unchecked")
    <M, ID> CachedModelsById<M, ID> getCacheById(Class<M> modelClass, Filter filterable) {
        requireNonNull(modelClass);
        String cacheKey = getCacheKey(modelClass, filterable);
        SoftReference<CachedModelsById<?, ?>> reference = cachesById.get(cacheKey);
        CachedModelsById<M, ID> holder = reference != null ? (CachedModelsById<M, ID>) reference.get() : null;
        return holder != null && !holder.isExpired() ? holder : null;
    }

    /**
     * Returns a cache with models by display name.
     *
     * @param modelClass the model class
     * @param <M>        the model type
     * @return the cached models
     */
    @SuppressWarnings("unchecked")
    <M> CachedModelsByDisplayValue<M> getCacheByDisplayName(Class<M> modelClass) {
        requireNonNull(modelClass);
        SoftReference<CachedModelsByDisplayValue<?>> reference = cachesByDisplayName.get(modelClass);
        CachedModelsByDisplayValue<M> holder = reference != null ? (CachedModelsByDisplayValue<M>) reference.get() : null;
        if (holder == null) {
            holder = new CachedModelsByDisplayValue<>(modelClass);
            cachesByDisplayName.put(modelClass, new SoftReference<>(holder));
        }
        return holder;
    }

    /**
     * Registers a model in the cache by display name.
     *
     * @param model       the model
     * @param displayName the display name
     * @param <M>         the model type
     */
    @SuppressWarnings("unchecked")
    <M> void registerByDisplayName(M model, String displayName) {
        if (displayName == null) return;
        requireNonNull(model);
        requireNonNull(displayName);
        CachedModelsByDisplayValue<M> cache = getCacheByDisplayName((Class<M>) model.getClass());
        cache.models.put(displayName.toLowerCase(), model);
    }

    /**
     * Returns a previously registered data repository.
     *
     * @param modelClass the model class
     * @param <M>        the model type
     * @param <ID>       the model identifier type
     * @return the repository, null if does not exist
     */
    @SuppressWarnings("unchecked")
    <M, ID> Repository<M, ID> getRepository(Class<M> modelClass) {
        requireNonNull(modelClass);
        Repository<M, ID> repository = (Repository<M, ID>) repositories.get(modelClass);
        if (repository == null) {
            Repositories repositories = new Repositories(getBeanFactory());
            repository = (Repository<M, ID>) repositories.getRepositoryFor(modelClass).orElse(null);
        }
        if (repository == null) {
            throw new DataSetException("A JPA repository for " + ClassUtils.getName(modelClass) + " is not registered");
        }
        return repository;
    }

    private void initialize() {
        discoverStaticFactories();
        discoverDynamicFactories();
        discoverDynamicLookups();
    }

    @SuppressWarnings("rawtypes")
    private void discoverStaticFactories() {
        LOGGER.debug("Discover static data set factories:");
        ServiceLoader<DataSetFactory> scannedFactories = ServiceLoader.load(DataSetFactory.class);
        for (DataSetFactory<?, ?, ?> scannedFactory : scannedFactories) {
            LOGGER.debug(" - " + ClassUtils.getName(scannedFactory));
            if (scannedFactory instanceof AbstractDataSetFactory abstractfactory) {
                abstractfactory.dataSetService = this;
            }
            factories.add(scannedFactory);
        }
        LOGGER.debug("Discovered " + factories.size() + " static data set factories");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void discoverDynamicFactories() {
        int staticFactoryCount = factories.size();
        LOGGER.debug("Discover dynamic data set factories:");
        Collection<Class<DataSet>> dataSetClasses = ClassUtils.resolveProviders(DataSet.class);
        for (Class<DataSet> dataSetClass : dataSetClasses) {
            LOGGER.debug(" - " + ClassUtils.getName(dataSetClass));
            factories.add(new ProviderDataSetFactory(dataSetClass));
        }
        LOGGER.info("Discovered " + (factories.size() - staticFactoryCount) + " dynamic data set factories");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void discoverDynamicLookups() {
        LOGGER.debug("Discover dynamic lookups:");
        Collection<Class<LookupProvider>> lookupProviderClasses = ClassUtils.resolveProviders(LookupProvider.class);
        for (Class<LookupProvider> lookupProviderClass : lookupProviderClasses) {
            LOGGER.debug(" - {}", ClassUtils.getName(lookupProviderClass));
            LookupProvider lookupProvider = ClassUtils.create(lookupProviderClass);
            lookupProviders.put(lookupProvider.getModel(), lookupProvider);
        }
        LOGGER.info("Discovered {} dynamic lookups", lookupProviders.size());
    }

    <M> String getCacheKey(Class<M> modelClass, Filter filterable) {
        String id = StringUtils.toIdentifier(ClassUtils.getName(modelClass)) + "_";
        id += filterable != null ? filterable.getHash() : Hashing.EMPTY;
        return id;
    }

    static class CachedModelsByDisplayValue<M> {

        private final Class<M> modelClass;
        private final Cache<String, M> models = CacheBuilder.newBuilder().maximumSize(1000).expireAfterWrite(Duration.ofSeconds(60)).build();

        CachedModelsByDisplayValue(Class<M> modelClass) {
            this.modelClass = modelClass;
        }
    }

    static class CachedModelsById<M, ID> {

        private final Class<M> modelClass;
        private final List<M> models;
        private final Map<ID, M> modelsById;
        private final Duration expiration;
        private final long created = System.currentTimeMillis();

        CachedModelsById(Class<M> modelClass, List<M> models, Map<ID, M> modelsById, Duration expiration) {
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
