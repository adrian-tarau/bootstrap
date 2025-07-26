package net.microfalx.bootstrap.web.chart.toolbar;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Svg {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String filename;

}
