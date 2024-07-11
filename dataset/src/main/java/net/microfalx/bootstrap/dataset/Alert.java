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
    private Integer minWidth;
    private Integer width;

    /**
     * Returns whether the alert has some properties changed.
     *
     * @return {@code true} if properties changed, {@code false}
     */
    public boolean hasProperties() {
        return minWidth != null || width != null;
    }

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
