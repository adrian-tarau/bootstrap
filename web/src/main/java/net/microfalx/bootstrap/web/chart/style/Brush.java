package net.microfalx.bootstrap.web.chart.style;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Brush {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean enabled;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean autoScaleYaxis;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String target;

}
