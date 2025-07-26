package net.microfalx.bootstrap.web.chart.style;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Marker {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double size;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String fillColor;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String strokeColor;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double strokeWidth;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String shape;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double offsetX;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double offsetY;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double radius;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String cssClass;

}
