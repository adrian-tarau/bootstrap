package net.microfalx.bootstrap.web.chart.zoom;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ZoomedArea {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Fill fill;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Stroke stroke;

}
