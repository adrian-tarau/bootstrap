package net.microfalx.bootstrap.web.chart.xaxis;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class Gradient {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String colorFrom;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String colorTo;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Double> stops;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double opacityFrom;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double opacityTo;

}
