package net.microfalx.bootstrap.web.chart.plot.radialbar;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;
import net.microfalx.bootstrap.web.chart.style.DropShadow;

@Data
@ToString
public class Track {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean show;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double startAngle;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double endAngle;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String background;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String strokeWidth;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double opacity;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double margin;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private DropShadow dropShadow;

}
