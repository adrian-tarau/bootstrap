package net.microfalx.bootstrap.web.chart.plot.hollow;

import com.fasterxml.jackson.annotation.JsonValue;

public enum HollowPosition {

    FRONT("front"),
    BACK("back");

    private final String value;

    HollowPosition(final String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
