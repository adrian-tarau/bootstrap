package net.microfalx.bootstrap.web.chart.legend;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ItemMargin {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double horizontal;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double vertical;

}
