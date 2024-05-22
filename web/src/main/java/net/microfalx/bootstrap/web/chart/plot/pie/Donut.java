package net.microfalx.bootstrap.web.chart.plot.pie;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Donut {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String size;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String background;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Labels labels;

}
