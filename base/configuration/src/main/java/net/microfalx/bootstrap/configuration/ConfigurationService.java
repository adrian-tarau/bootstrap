package net.microfalx.bootstrap.configuration;

import lombok.extern.slf4j.Slf4j;
import net.microfalx.bootstrap.registry.Data;
import net.microfalx.bootstrap.registry.Registry;
import net.microfalx.bootstrap.registry.RegistryService;
import net.microfalx.threadpool.ThreadPool;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static net.microfalx.bootstrap.configuration.ConfigurationUtils.ROOT_METADATA_ID;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.EMPTY_STRING;
import static net.microfalx.lang.StringUtils.toIdentifier;

@Slf4j
@Service
public class ConfigurationService implements InitializingBean {

    private Configuration configuration;

    @Autowired
    private RegistryService registryService;

    @Autowired
    private Environment environment;

    private final Map<String, Metadata> metadatas = new ConcurrentHashMap<>();

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
     * Returns an application property
     *
     * @param key the name of the property
     * @return the value
     */
    public String getProperty(String key) {
        requireNonNull(key);
        return environment.getProperty(key);
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

    @Override
    public void afterPropertiesSet() throws Exception {
        configuration = new SubsetImpl(this, null, EMPTY_STRING, "Root");
        loadMetadata();
        ThreadPool.get().execute(this::registerMetadata);
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
        data.setAttribute("key", metadata.getFullKey());
        data.setAttribute("name", metadata.getName());
        data.set(getProperty(metadata.getFullKey()));
        registry.set(data);
        return true;
    }
}
