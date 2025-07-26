package net.microfalx.bootstrap.web.chart.style;

import com.fasterxml.jackson.annotation.JsonValue;

public enum LineCap {

    BUTT("butt"),
    SQUARE("square"),
    ROUND("round");

    private final String value;

    LineCap(final String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
