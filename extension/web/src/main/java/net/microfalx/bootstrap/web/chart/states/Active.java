package net.microfalx.bootstrap.web.chart.states;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Active {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean allowMultipleDataPointsSelection;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Filter filter;


}
