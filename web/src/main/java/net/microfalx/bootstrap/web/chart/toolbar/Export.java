package net.microfalx.bootstrap.web.chart.toolbar;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Export {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Csv csv;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Svg svg;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Png png;

}
