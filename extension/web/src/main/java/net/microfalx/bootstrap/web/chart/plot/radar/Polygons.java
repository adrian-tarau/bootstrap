package net.microfalx.bootstrap.web.chart.plot.radar;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;
import net.microfalx.bootstrap.web.chart.plot.polygons.Fill;

import java.util.List;

@Data
@ToString
public class Polygons {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> strokeColor;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> connectorColors;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Fill fill;

}
