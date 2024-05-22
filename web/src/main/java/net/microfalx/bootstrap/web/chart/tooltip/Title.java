package net.microfalx.bootstrap.web.chart.tooltip;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import net.microfalx.bootstrap.web.chart.Function;

@Data
@AllArgsConstructor
@ToString
public class Title {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Function formatter;

}
