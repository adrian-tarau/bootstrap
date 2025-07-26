package net.microfalx.bootstrap.web.chart.plot.bar;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class DataLabels {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String position;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer maxItems;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private boolean hideOverflowingLabels;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String orientation;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Total total;

}
