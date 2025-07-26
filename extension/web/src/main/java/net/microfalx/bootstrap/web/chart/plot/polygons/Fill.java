package net.microfalx.bootstrap.web.chart.plot.polygons;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class Fill {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> colors;

}
