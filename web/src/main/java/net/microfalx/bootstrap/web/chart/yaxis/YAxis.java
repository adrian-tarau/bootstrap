package net.microfalx.bootstrap.web.chart.yaxis;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class YAxis {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean show;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean showAlways;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean showForNullSeries;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String seriesName;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean opposite;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean reversed;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean logarithmic;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double logBase;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double tickAmount;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean forceNiceScale;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Object min;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Object max;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean floating;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double decimalsInFloat;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double stepSize;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Labels labels;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private AxisBorder axisBorder;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private AxisTicks axisTicks;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Title title;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Crosshairs crosshairs;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Tooltip tooltip;


}
