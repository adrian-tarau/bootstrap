package net.microfalx.bootstrap.web.chart.annotations;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class AnnotationStyle {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String background;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String color;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String fontSize;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String cssClass;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Padding padding;

}
