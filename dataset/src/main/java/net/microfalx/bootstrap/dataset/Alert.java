package net.microfalx.bootstrap.dataset;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * A class which carries additional information about a field.
 */
@Builder
@ToString
@Getter
public class Alert {

    private Type type = Type.INFO;
    private String message;

    public enum Type {
        PRIMARY,
        SECONDARY,
        SUCCESS,
        DANGER,
        WARNING,
        INFO,
        LIGHT,
        DARK
    }
}
