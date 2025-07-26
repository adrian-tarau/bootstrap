package net.microfalx.bootstrap.web.chart.plot.xmap;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Ranges {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double from;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double to;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String color;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String foreColor;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String name;

}
