package net.microfalx.bootstrap.web.chart.plot.boxplot;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Colors {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String upper;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String lower;

}
