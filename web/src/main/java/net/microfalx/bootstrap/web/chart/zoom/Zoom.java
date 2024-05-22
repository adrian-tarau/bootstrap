package net.microfalx.bootstrap.web.chart.zoom;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Zoom {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean enabled;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ZoomType type;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ZoomedArea zoomedArea;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean autoScaleYaxis;

}