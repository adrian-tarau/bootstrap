package net.microfalx.bootstrap.web.chart.legend;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Shape {
    CIRCLE("circle"),
    SQUARE("square");

    private final String value;

    Shape(final String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
