package net.microfalx.bootstrap.web.chart.xaxis;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;
import net.microfalx.bootstrap.web.chart.style.DropShadow;

@Data
@ToString
public class Crosshairs {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean show;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String width;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String position;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double opacity;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Stroke stroke;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Fill fill;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private DropShadow dropShadow;

}
