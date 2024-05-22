package net.microfalx.bootstrap.web.chart.theme;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Monochrome {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean enabled;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String color;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ShadeTo shadeTo;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double shadeIntensity;

}
