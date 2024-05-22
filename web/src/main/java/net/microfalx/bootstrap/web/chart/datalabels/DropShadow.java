package net.microfalx.bootstrap.web.chart.datalabels;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class DropShadow {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean enable;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double top;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double left;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double blur;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double opacity;

}
