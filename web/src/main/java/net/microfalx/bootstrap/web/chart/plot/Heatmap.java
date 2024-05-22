package net.microfalx.bootstrap.web.chart.plot;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Heatmap extends Treemap {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double radius;

}
