package net.microfalx.bootstrap.web.chart.plot.xmap;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class ColorScale {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Ranges> ranges;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean inverse;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double min;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double max;

}
