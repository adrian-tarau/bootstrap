package net.microfalx.bootstrap.web.chart.selection;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Axis {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double min;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double max;

}
