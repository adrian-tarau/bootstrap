package net.microfalx.bootstrap.web.chart.plot.pie;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class DataLabels {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double offset;

}
