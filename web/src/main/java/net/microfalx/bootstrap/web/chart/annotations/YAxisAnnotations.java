package net.microfalx.bootstrap.web.chart.annotations;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class YAxisAnnotations {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double y;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double y2;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double strokeDashArray;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String fillColor;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String borderColor;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double opacity;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double offsetX;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double offsetY;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double yAxisIndex;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private AnnotationLabel label;

}
