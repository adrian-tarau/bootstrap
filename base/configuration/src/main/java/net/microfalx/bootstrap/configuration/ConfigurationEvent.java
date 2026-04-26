package net.microfalx.bootstrap.configuration;

import lombok.Getter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

/**
 * An event triggered when configuration changed.
 */
@Getter
@ToString
public class ConfigurationEvent extends ApplicationEvent {

    private Type type;
    private final String key;
    private final String previousValue;
    private final String currentValue;

    public ConfigurationEvent(Configuration configuration, Type type, String key) {
        super(configuration);
        this.type = type;
        this.key = key;
        this.previousValue = null;
        this.currentValue = null;
    }

    public ConfigurationEvent(Configuration configuration, Type type, String key, String previousValue, String currentValue) {
        super(configuration);
        this.key = key;
        this.previousValue = previousValue;
        this.currentValue = currentValue;
    }

    public enum Type {
        PROPERTY,
        GROUP
    }

}
