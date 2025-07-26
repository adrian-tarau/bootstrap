package net.microfalx.bootstrap.web.chart.plot.candlestick;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Colors {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String upward;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String downward;

}
