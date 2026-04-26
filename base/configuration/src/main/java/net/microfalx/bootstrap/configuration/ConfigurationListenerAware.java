package net.microfalx.bootstrap.configuration;

/**
 * An interface which gives an object the capability to provide configuration listener.
 */
public interface ConfigurationListenerAware {

    /**
     * Adds a new configuration listener
     *
     * @param listener the listener to register
     */
    void addListener(ConfigurationListener listener);
}
