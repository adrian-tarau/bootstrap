package net.microfalx.bootstrap.web.chart.annotations;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Label {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String borderColor;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double borderWidth;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String text;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String textAnchor;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String position;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String orientation;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double offsetX;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double offsetY;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private AnnotationStyle style;

}
