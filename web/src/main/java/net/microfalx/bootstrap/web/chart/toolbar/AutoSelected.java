package net.microfalx.bootstrap.web.chart.toolbar;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AutoSelected {

    ZOOM("zoom"),
    SELECTION("selection"),
    PAN("pan");

    private final String value;

    AutoSelected(final String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
