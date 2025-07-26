package net.microfalx.bootstrap.web.chart.toolbar;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Toolbar {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean show;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Tools tools;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Export export;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private AutoSelected autoSelected;

}
