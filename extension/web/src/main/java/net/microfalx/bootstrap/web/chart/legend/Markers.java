package net.microfalx.bootstrap.web.chart.legend;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Markers {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double width;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double height;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String strokeColor;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double strokeWidth;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double offsetX;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double offsetY;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double radius;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Shape shape;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String customHTML;

}
