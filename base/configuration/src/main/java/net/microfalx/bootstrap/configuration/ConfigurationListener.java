package net.microfalx.bootstrap.configuration;

/**
 * A listener used to notify services of configuration changes.
 */
public interface ConfigurationListener {

    /**
     * Invoked when the configuration changes.
     *
     * @param event the event
     */
    void onEvent(ConfigurationEvent event);
}
