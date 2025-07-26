package net.microfalx.bootstrap.web.chart.grid;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class Row {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> colors;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double opacity;

}
