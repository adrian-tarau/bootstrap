package net.microfalx.bootstrap.configuration;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;

/**
 * A configuration listener which notifies only when a configuration key with a given prefix changes.
 */
public abstract class PrefixConfigurationListener implements ConfigurationListener {

    private final String prefix;
    private final ConfigurationEvent.Type type;

    public PrefixConfigurationListener(String prefix) {
        this(prefix, ConfigurationEvent.Type.GROUP);
    }

    public PrefixConfigurationListener(String prefix, ConfigurationEvent.Type type) {
        requireNotEmpty(prefix);
        requireNonNull(type);
        this.prefix = prefix.toLowerCase();
        this.type = type;
    }

    @Override
    public final void onEvent(ConfigurationEvent event) {
        // must be the same type of event
        if (event.getType() != this.type) return;
        // key must start with expected prefix
        if (!event.getKey().toLowerCase().startsWith(prefix)) return;
        onPrefixChange(event);
    }

    /**
     * Invoked when an event is received for the configured prefix.
     *
     * @param event the event
     */
    protected abstract void onPrefixChange(ConfigurationEvent event);
}
