package net.microfalx.bootstrap.configuration;

import lombok.AccessLevel;
import lombok.Getter;
import net.microfalx.bootstrap.registry.Data;
import net.microfalx.bootstrap.registry.Registry;
import net.microfalx.lang.EncryptionUtils;
import net.microfalx.lang.ObjectUtils;
import net.microfalx.lang.SecretUtils;
import net.microfalx.lang.StringUtils;

import java.util.Optional;
import java.util.Set;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.StringUtils.isNotEmpty;

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
        return get(getFinalKey(key), null);
    }

    @Override
    public String get(String key, String defaultValue) {
        requireNotEmpty(key);
        String registryKey = getRegistryKey(key);
        Optional<Data> data = getRegistry().get(registryKey);
        String value;
        if (data.isPresent()) {
            value = ObjectUtils.toString(data.get().get());
        } else {
            value = getProperty(key);
        }
        if (SecretUtils.isSecret(key) && EncryptionUtils.isEncrypted(key)) {
            value = EncryptionUtils.decrypt(value);
        }
        return value != null ? value : defaultValue;
    }

    @Override
    public boolean get(String key, boolean defaultValue) {
        String value = get(key);
        return StringUtils.asBoolean(value, defaultValue);
    }

    @Override
    public int get(String key, int defaultValue) {
        String value = get(key);
        try {
            return isNotEmpty(value) ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public long get(String key, long defaultValue) {
        String value = get(key);
        try {
            return isNotEmpty(value) ? Long.parseLong(value) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public float get(String key, float defaultValue) {
        String value = get(key);
        try {
            return isNotEmpty(value) ? Float.parseFloat(value) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public double get(String key, double defaultValue) {
        String value = get(key);
        try {
            return isNotEmpty(value) ? Double.parseDouble(value) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public Subset at(String prefix) {
        Metadata metadata = configurationService.getMetadata(prefix);
        return new SubsetImpl(configurationService, this, prefix, metadata.getName());
    }

    @Override
    public void set(String key, String value) {
        requireNotEmpty(key);
        String registryKey = getRegistryKey(key);
        Data data = getRegistry().getOrCreate(registryKey);
        data.set(value);
        getRegistry().set(data);
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
