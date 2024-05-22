package net.microfalx.bootstrap.web.chart.plot;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;
import net.microfalx.bootstrap.web.chart.plot.bar.Colors;
import net.microfalx.bootstrap.web.chart.plot.bar.DataLabels;

@Data
@ToString
public class Bar {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean horizontal;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String columnWidth;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String barHeight;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean distributed;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Colors colors;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private DataLabels dataLabels;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean rangeBarGroupRows;

}
