package net.microfalx.bootstrap.web.chart;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
public class Sparkline {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean enabled;

}
