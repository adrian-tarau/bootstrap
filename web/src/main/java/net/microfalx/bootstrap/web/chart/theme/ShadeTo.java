package net.microfalx.bootstrap.web.chart.theme;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ShadeTo {

    LIGHT("light"),
    DARK("dark");

    private final String value;

    ShadeTo(final String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
