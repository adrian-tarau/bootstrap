package net.microfalx.bootstrap.web.chart.plot;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;
import net.microfalx.bootstrap.web.chart.plot.radar.Polygons;

@Data
@ToString
public class Radar {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double size;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double offsetX;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double offsetY;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Polygons polygons;

}
