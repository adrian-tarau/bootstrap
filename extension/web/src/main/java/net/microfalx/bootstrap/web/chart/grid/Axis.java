package net.microfalx.bootstrap.web.chart.grid;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Axis {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Lines lines;
}
