package net.microfalx.bootstrap.web.chart.style;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Curve {

    SMOOTH("smooth"),
    STRAIGHT("straight"),
    STEPLINE("stepline");

    private final String value;

    Curve(final String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
