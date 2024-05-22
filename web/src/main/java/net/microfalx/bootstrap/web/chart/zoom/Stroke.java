package net.microfalx.bootstrap.web.chart.zoom;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Stroke {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String color;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double opacity;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double width;

}
