package net.microfalx.bootstrap.web.chart.tooltip;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Tooltip {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean enabled;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean shared;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean followCursor;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean intersect;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean inverseOrder;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private OnDatasetHover onDatasetHover;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private X x;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Y y;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Z z;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Marker marker;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Items items;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Fixed fixed;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String custom;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean fillSeriesColor;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String theme;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Style style;

    public static Tooltip disable() {
        return new Tooltip().setEnabled(false);
    }

    public static Tooltip onlyValue() {
        Tooltip tooltip = new Tooltip();
        tooltip.setFixed(new Fixed(false));
        tooltip.setX(X.hide());
        tooltip.setY(Y.noTitle());
        return tooltip;
    }

    public static Tooltip valueWithTimestamp() {
        Tooltip tooltip = new Tooltip();
        tooltip.setFixed(new Fixed(false));
        tooltip.setX(X.timestamp());
        tooltip.setY(Y.noTitle());
        return tooltip;
    }

}
