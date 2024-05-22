package net.microfalx.bootstrap.web.chart.plot;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;
import net.microfalx.bootstrap.web.chart.plot.xmap.ColorScale;

@Data
@ToString
public class Treemap {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean enableShades;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double shadeIntensity;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean reverseNegativeShade;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean distributed;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean useFillColorAsStroke;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ColorScale colorScale;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double radius;

}
