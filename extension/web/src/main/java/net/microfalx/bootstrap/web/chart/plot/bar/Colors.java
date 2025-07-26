package net.microfalx.bootstrap.web.chart.plot.bar;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class Colors {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Ranges> ranges;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> backgroundBarColors;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double backgroundBarOpacity;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double backgroundBarRadius;
}
