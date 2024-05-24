package net.microfalx.bootstrap.web.chart.xaxis;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

@Data
@ToString
public class XAxis {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private XAxisType type;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> categories;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private AxisBorder axisBorder;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private AxisTicks axisTicks;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal tickAmount;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private TickPlacement tickPlacement;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double min;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double max;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double range;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean floating;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String position;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Title title;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Crosshairs crosshairs;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Tooltip tooltip;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Labels labels;

    public static XAxis dateTime() {
        return new XAxis().setType(XAxisType.DATETIME);
    }

}
