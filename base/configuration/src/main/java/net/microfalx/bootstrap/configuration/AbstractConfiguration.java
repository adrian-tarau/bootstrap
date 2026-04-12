package net.microfalx.bootstrap.configuration;

import lombok.AccessLevel;
import lombok.Getter;
import net.microfalx.bootstrap.registry.Data;
import net.microfalx.bootstrap.registry.Registry;
import net.microfalx.lang.ObjectUtils;
import net.microfalx.lang.StringUtils;

import java.util.Optional;
import java.util.Set;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Base class for all configuration classes.
 */
abstract class AbstractConfiguration implements Configuration {

    @Getter(AccessLevel.PROTECTED)
    private final ConfigurationService configurationService;
    private final Configuration parent;

    AbstractConfiguration(ConfigurationService configurationService, Configuration parent) {
        requireNonNull(configurationService);
        this.configurationService = configurationService;
        this.parent = parent;
    }

    @Override
    public final Configuration getParent() {
        return parent;
    }

    @Override
    public Set<String> getKeys() {
        return Set.of();
    }

    @Override
    public String get(String key) {
        return get(key, null);
    }

    @Override
    public String get(String key, String defaultValue) {
        String registryKey = getRegistryKey(key);
        Optional<Data> data = getRegistry().get(registryKey);
        String value;
        if (data.isPresent()) {
            value = ObjectUtils.toString(data.get().get());
        } else {
            value = getProperty(key);
        }
        return value != null ? value : defaultValue;
    }

    @Override
    public int get(String key, boolean defaultValue) {
        return 0;
    }

    @Override
    public int get(String key, int defaultValue) {
        return 0;
    }

    @Override
    public long get(String key, long defaultValue) {
        return 0;
    }

    @Override
    public float get(String key, float defaultValue) {
        return 0;
    }

    @Override
    public double get(String key, double defaultValue) {
        return 0;
    }

    @Override
    public Subset at(String prefix) {
        return null;
    }

    protected final Registry getRegistry() {
        return configurationService.getRegistry();
    }

    protected String getFinalKey(String key) {
        return key;
    }

    protected String getProperty(String key) {
        return configurationService.getProperty(key);
    }

    protected final String getRegistryKey(String key) {
        return ConfigurationUtils.REGISTRY_PATH + "/" + StringUtils.toIdentifier(key);
    }
}
