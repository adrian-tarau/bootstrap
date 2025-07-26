package net.microfalx.bootstrap.web.chart.style;

import com.fasterxml.jackson.annotation.JsonValue;

public enum HorizontalAlign {

    LEFT("left"),
    RIGHT("right"),
    CENTER("center");

    private final String value;

    HorizontalAlign(final String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
