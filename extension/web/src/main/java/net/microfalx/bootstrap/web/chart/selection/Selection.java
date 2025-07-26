package net.microfalx.bootstrap.web.chart.selection;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Selection {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean enabled;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String type;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Fill fill;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Stroke stroke;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Axis xaxis;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Axis yaxis;

}
