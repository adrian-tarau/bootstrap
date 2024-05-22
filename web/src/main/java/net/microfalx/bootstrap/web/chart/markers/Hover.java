package net.microfalx.bootstrap.web.chart.markers;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Hover {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double size;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double sizeOffset;

}
