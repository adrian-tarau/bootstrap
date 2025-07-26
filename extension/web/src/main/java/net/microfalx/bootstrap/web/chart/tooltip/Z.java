package net.microfalx.bootstrap.web.chart.tooltip;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;
import net.microfalx.bootstrap.web.chart.Function;

@Data
@ToString
public class Z {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Function formatter;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String title;

}
