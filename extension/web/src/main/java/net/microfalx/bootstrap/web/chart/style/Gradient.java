package net.microfalx.bootstrap.web.chart.style;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class Gradient {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String shade;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String type;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double shadeIntensity;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> gradientToColors;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean inverseColors;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double opacityFrom;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double opacityTo;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Double> stops;

}
