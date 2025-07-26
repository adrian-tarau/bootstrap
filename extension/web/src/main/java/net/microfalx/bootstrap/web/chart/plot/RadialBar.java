package net.microfalx.bootstrap.web.chart.plot;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;
import net.microfalx.bootstrap.web.chart.plot.radialbar.Hollow;
import net.microfalx.bootstrap.web.chart.plot.radialbar.RadialBarDataLabels;
import net.microfalx.bootstrap.web.chart.plot.radialbar.Track;

@Data
@ToString
public class RadialBar {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double size;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean inverseOrder;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double startAngle;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double endAngle;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double offsetX;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double offsetY;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Hollow hollow;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Track track;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private RadialBarDataLabels dataLabels;

}
