package net.microfalx.bootstrap.web.chart.annotations;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Padding {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double left;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double right;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double top;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double bottom;

}
