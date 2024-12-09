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

    @Builder.Default
    private Type type = Type.INFO;
    private String message;
    private Integer minWidth;
    private Integer width;
    @Builder.Default
    private Icon icon = Icon.EXCLAMATION;
    private boolean tooltip;

    /**
     * Returns whether the alert has some properties changed.
     *
     * @return {@code true} if properties changed, {@code false}
     */
    public boolean hasProperties() {
        return minWidth != null || width != null;
    }

    public enum Icon {
        BELL,
        EXCLAMATION,
        INFORMATION,
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
