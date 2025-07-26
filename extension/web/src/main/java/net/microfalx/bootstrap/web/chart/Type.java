package net.microfalx.bootstrap.web.chart;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Type {

    LINE("line"),
    AREA("area"),
    BAR("bar"),
    HISTOGRAM("histogram"),
    PIE("pie"),
    DONUT("donut"),
    RADIAL_BAR("radialBar"),
    SCATTER("scatter"),
    BUBBLE("bubble"),
    HEATMAP("heatmap"),
    CANDLESTICK("candlestick"),
    RADAR("radar"),
    RANGE_BAR("rangeBar"),
    BOXPLOT("boxPlot"),
    TREEMAP("treemap");

    private final String value;

    Type(final String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
