package net.microfalx.bootstrap.web.chart.style;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Pattern {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String style;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double width;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double height;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double strokeWidth;

}
