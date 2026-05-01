package net.microfalx.bootstrap.configuration;

import lombok.AccessLevel;
import lombok.Getter;
import net.microfalx.bootstrap.registry.Registry;
import net.microfalx.lang.StringUtils;

import java.util.Set;
import java.util.stream.Collectors;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.StringUtils.isEmpty;
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
        int prefixLength = getPrefix().length() + 1;
        return configurationService.getEntries(getPrefix()).stream()
                .map(Metadata::getFullKey)
                .map(s -> s.substring(prefixLength))
                .collect(Collectors.toSet());
    }

    @Override
    public String get(String key) {
        return get(key, null);
    }

    @Override
    public String get(String key, String defaultValue) {
        requireNotEmpty(key);
        return configurationService.getFromRegistry(this, getFinalKey(key), defaultValue);
    }

    @Override
    public <T> T get(String key, Class<T> type, Object defaultValue) {
        String value = get(key, null);
        T result;
        if (isEmpty(value)) {
            result = configurationService.convert(key, defaultValue, type);
        } else {
            result = configurationService.convert(key, value, type);
        }
        return result;
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
    public <T> void set(String key, T value) {
        requireNotEmpty(key);
        configurationService.setToRegistry(this, getFinalKey(key), value);
    }

    @Override
    public void addListener(ConfigurationListener listener) {
        configurationService.addListener(listener);
    }

    protected final Registry getRegistry() {
        return configurationService.getRegistry();
    }

    protected String getFinalKey(String key) {
        return key;
    }

    protected String getPrefix() {
        return StringUtils.EMPTY_STRING;
    }

    protected String getProperty(String key) {
        return configurationService.getProperty(key);
    }

}
