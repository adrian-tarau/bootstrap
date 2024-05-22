package net.microfalx.bootstrap.web.chart.plot;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;
import net.microfalx.bootstrap.web.chart.plot.pie.DataLabels;
import net.microfalx.bootstrap.web.chart.plot.pie.Donut;

@Data
@ToString
public class Pie {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double size;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double customScale;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double offsetX;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double offsetY;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double startAngle;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double endAngle;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean expandOnClick;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private DataLabels dataLabels;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Donut donut;

}
