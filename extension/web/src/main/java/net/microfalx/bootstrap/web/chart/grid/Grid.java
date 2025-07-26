package net.microfalx.bootstrap.web.chart.grid;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Grid {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean show;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String borderColor;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double strokeDashArray;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Position position;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Axis xaxis;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Axis yaxis;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Row row;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Column column;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Padding padding;

}
