package net.microfalx.bootstrap.web.chart.grid;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Position {

    FRONT("front"),
    BACK("back");

    private final String value;

    Position(final String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
