package net.microfalx.bootstrap.web.chart.plot.radialbar;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RadialBarDataLabels {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean show;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Name name;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Value value;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Total total;

}
