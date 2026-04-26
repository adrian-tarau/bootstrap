package net.microfalx.bootstrap.configuration;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.microfalx.bootstrap.registry.Data;
import net.microfalx.bootstrap.registry.Registry;
import net.microfalx.bootstrap.registry.RegistryService;
import net.microfalx.lang.*;
import net.microfalx.threadpool.ThreadPool;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.lang.System.currentTimeMillis;
import static net.microfalx.bootstrap.configuration.ConfigurationUtils.ROOT_METADATA_ID;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ExceptionUtils.getRootCauseDescription;
import static net.microfalx.lang.StringUtils.*;

@SuppressWarnings("unchecked")
@Slf4j
@Service
public class ConfigurationService implements InitializingBean {

    private Configuration configuration;

    @Autowired private ApplicationEventPublisher eventPublisher;
    @Autowired private RegistryService registryService;
    @Autowired private Environment environment;
    @Autowired private ConversionService conversionService;
    @Autowired private ThreadPool threadPool;

    private Duration cacheExpiration = Duration.ofSeconds(5);
    private Binder binder;
    private final Map<String, Metadata> metadatas = new ConcurrentHashMap<>();
    private final Map<String, CachedValue> cachedValues = new ConcurrentHashMap<>();
    private final Collection<ConfigurationListener> listeners = new CopyOnWriteArrayList<>();

    /**
     * Returns the registry used to store the configuration.
     *
     * @return a non-null instance
     */
    public Registry getRegistry() {
        return registryService.getRegistry();
    }

    /**
     * Returns the root configuration.
     *
     * @return a non-null instance
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Registers a configuration listener.
     *
     * @param listener a non-null instance
     */
    public void addListener(ConfigurationListener listener) {
        requireNonNull(listener);
        listeners.add(listener);
    }

    /**
     * Returns an application property
     *
     * @param key the name of the property
     * @return the value
     */
    public String getProperty(String key) {
        requireNonNull(key);
        try {
            ConfigurationPropertyName name = ConfigurationPropertyName.adapt(key, '.');
            Bindable<String> bindable = Bindable.of(String.class);
            return binder.bindOrCreate(name, bindable, BindHandler.DEFAULT);
        } catch (Exception e) {
            LOGGER.warn("Failed to get the property '{}', root cause: {}", key, getRootCauseDescription(e));
            return null;
        }
    }

    /**
     * Returns the root metadata.
     *
     * @return a non-null instance
     */
    public Metadata getRootMetadata() {
        return metadatas.get(toIdentifier(ROOT_METADATA_ID));
    }

    /**
     * Returns metadata for the given key.
     *
     * @param key the configuration key
     * @return a non-null instance
     */
    public Metadata getMetadata(String key) {
        requireNonNull(key);
        Metadata metadata = this.metadatas.get(toIdentifier(key));
        if (metadata == null) {
            metadata = new Metadata(null, key, ConfigurationUtils.getTitle(key));
            this.metadatas.put(metadata.getId(), metadata);
        }
        return metadata;
    }

    /**
     * Registers metadata associated with the configuration entry.
     *
     * @param metadata the metadata to register
     */
    public void registerMetadata(Metadata metadata) {
        requireNonNull(metadata);
        this.metadatas.put(metadata.getId(), metadata);
    }

    /**
     * Notifies listeners that a group (all properties under the group) changed.
     *
     * @param metadata the metadata of the group
     */
    public void notifyGroupChange(Metadata metadata) {
        requireNonNull(metadata);
        ConfigurationEvent event = new ConfigurationEvent(configuration, ConfigurationEvent.Type.GROUP, metadata.getFullKey());
        fireConfigurationEvent(event);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        configuration = new SubsetImpl(this, null, EMPTY_STRING, "Root");
        loadMetadata();
        initBinder();
        threadPool.execute(this::registerMetadata);
    }

    void propertyChanged(Configuration configuration, String key, String previousValue, String currentValue) {
        ConfigurationEvent event = new ConfigurationEvent(configuration, ConfigurationEvent.Type.PROPERTY, key, previousValue, currentValue);
        fireConfigurationEvent(event);
    }

    <T> T convert(String key, Object value, Class<T> type) {
        if (value == null) return null;
        if (type == Duration.class) {
            return (T) TimeUtils.parseDuration(value.toString());
        } else {
            try {
                return conversionService.convert(value, type);
            } catch (Exception e) {
                throw new ConfigurationException("Failed to convert value '" + value + "' to "
                        + ClassUtils.getName(type) + " for key '" + key + "'", e);
            }
        }
    }

    String getFromRegistry(Configuration configuration, String key, String defaultValue) {
        String value = getFromCache(key);
        if (StringUtils.isEmpty(value)) {
            String registryKey = getRegistryKey(key);
            Optional<Data> data = getRegistry().get(registryKey);
            if (data.isPresent()) {
                value = ObjectUtils.toString(data.get().get());
            } else {
                value = getProperty(key);
            }
            cachedValues.put(key, new CachedValue(value));
        }
        if (SecretUtils.isSecret(key) && EncryptionUtils.isEncrypted(key)) {
            value = EncryptionUtils.decrypt(value);
        }
        return defaultIfNull(value, defaultValue);
    }

    void setToRegistry(Configuration configuration, String key, Object value) {
        String registryKey = getRegistryKey(key);
        Data data = getRegistry().getOrCreate(registryKey);
        String previousValue = ObjectUtils.toString(data.get());
        data.set(value);
        getRegistry().set(data);
        propertyChanged(configuration, key, previousValue, ObjectUtils.toString(value));
    }

    private void initBinder() {
        binder = Binder.get(environment);
    }

    private void loadMetadata() {
        ConfigurationLoader loader = new ConfigurationLoader();
        loader.load();
        this.metadatas.putAll(loader.getMetadata());
        LOGGER.info("Loaded {} configuration groups with {} items", loader.getGroupCount(), loader.getItemCount());
    }

    private void registerMetadata() {
        Registry registry = getRegistry();
        int registered = 0;
        for (Metadata metadata : metadatas.values()) {
            try {
                if (registerMetadata(registry, metadata)) registered++;
            } catch (Exception e) {
                LOGGER.atError().setCause(e).log("Failed to register metadata {} in registry", metadata.getFullKey());
            }
        }
        LOGGER.info("Registered {} new configuration entries in registry", registered);
    }

    private boolean registerMetadata(Registry registry, Metadata metadata) {
        Data data = registry.getOrCreate(ConfigurationUtils.getRegistryPath(metadata));
        if (data.exists() || !metadata.isLeaf()) return false;
        boolean isSecret = SecretUtils.isSecret(metadata.getFullKey());
        data.setAttribute("key", metadata.getFullKey());
        data.setAttribute("name", metadata.getName());
        String value = getProperty(metadata.getFullKey());
        value = isSecret && !EncryptionUtils.isEncrypted(value) ? EncryptionUtils.encrypt(value) : value;
        data.set(value);
        registry.set(data);
        return true;
    }

    private String getFromCache(String key) {
        CachedValue cachedValue = cachedValues.get(key);
        if (cachedValue != null && !cachedValue.isExpired(cacheExpiration)) {
            return cachedValue.getValue();
        } else {
            return null;
        }
    }

    private String getRegistryKey(String key) {
        return ConfigurationUtils.REGISTRY_PATH + "/" + StringUtils.toIdentifier(key);
    }

    void fireConfigurationEvent(ConfigurationEvent event) {
        eventPublisher.publishEvent(event);
        for (ConfigurationListener listener : listeners) {
            listener.onEvent(event);
        }
    }

    @Getter
    @ToString
    private static class CachedValue {

        private final String value;
        private final long created = currentTimeMillis();

        private CachedValue(String value) {
            this.value = value;
        }

        boolean isExpired(Duration expiration) {
            return TimeUtils.millisSince(currentTimeMillis()) > expiration.toMillis();
        }
    }
}
