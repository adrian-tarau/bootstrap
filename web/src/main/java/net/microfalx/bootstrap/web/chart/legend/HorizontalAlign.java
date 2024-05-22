package net.microfalx.bootstrap.web.chart.legend;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.ToString;

@ToString
public enum HorizontalAlign {

    LEFT("left"),
    CENTER("center"),
    RIGHT("right");

    private final String value;

    HorizontalAlign(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
