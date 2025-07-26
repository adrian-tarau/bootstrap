package net.microfalx.bootstrap.web.chart.toolbar;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Tools {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String download;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String selection;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String zoom;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String zoomin;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String zoomout;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String pan;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String reset;

}
