package net.microfalx.bootstrap.web.chart.theme;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Theme {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Mode mode;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String palette;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Monochrome monochrome;

}
