package net.microfalx.bootstrap.web.chart.states;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class States {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Normal normal;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Hover hover;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Active active;

}