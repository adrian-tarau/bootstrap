package net.microfalx.bootstrap.web.chart.markers;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class DiscretePoint {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double seriesIndex;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double dataPointIndex;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String fillColor;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String strokeColor;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double size;

}
