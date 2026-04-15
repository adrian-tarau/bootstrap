package net.microfalx.bootstrap.configuration;

import java.util.Set;

/**
 * An interface which exposes the configuration tree and allows services to retrieve and persist
 * configuration entries.
 */
public interface Configuration {

    /**
     * Returns all available keys in the configuration (or subset).
     *
     * @return a non-null instance
     */
    Set<String> getKeys();

    /**
     * Returns the String value for a given key.
     *
     * @param key the key
     * @return the value, default is missing
     */
    String get(String key);

    /**
     * Returns the String value for a given key.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the value, default is missing
     */
    String get(String key, String defaultValue);

    /**
     * Returns the integer value for a given key.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the value, default is missing
     */
    boolean get(String key, boolean defaultValue);

    /**
     * Returns the integer value for a given key.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the value, default is missing
     */
    int get(String key, int defaultValue);

    /**
     * Returns the long value for a given key.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the value, default is missing
     */
    long get(String key, long defaultValue);

    /**
     * Returns the float value for a given key.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the value, default is missing
     */
    float get(String key, float defaultValue);

    /**
     * Returns the double value for a given key.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the value, default is missing
     */
    double get(String key, double defaultValue);

    /**
     * Changes a configuration entry. The change is persisted and will be available for other services.
     *
     * @param key   the key
     * @param value the value
     */
    void set(String key, String value);

    /**
     * Returns the parent configuration.
     *
     * @return the parent, null if root subset
     */
    Configuration getParent();

    /**
     * Creates a subset from this configuration.
     * <p>
     * The new configuration prefix is the prefix of the current configuration plus the provided prefix.
     *
     * @param prefix the configuration prefix/key to be added as a prefix
     * @return a non-null instance
     */
    Subset at(String prefix);
}
