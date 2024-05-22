package net.microfalx.bootstrap.web.chart.legend;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
public class OnItemClick {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean toggleDataSeries;

}
