package net.microfalx.bootstrap.web.chart.datalabels;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;
import net.microfalx.bootstrap.web.chart.Function;
import net.microfalx.bootstrap.web.chart.style.DropShadow;

import java.util.List;

@Data
@ToString
public class DataLabels {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean enabled;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Double> enabledOnSeries;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Function formatter;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private TextAnchor textAnchor;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double offsetX;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double offsetY;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Style style;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private DropShadow dropShadow;

    public static DataLabels disabled() {
        return new DataLabels().setEnabled(false);
    }

}
