package net.microfalx.bootstrap.web.chart;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Responsive {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double breakpoint;

}
