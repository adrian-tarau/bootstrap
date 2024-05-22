package net.microfalx.bootstrap.web.chart.nodata;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;
import net.microfalx.bootstrap.web.chart.style.HorizontalAlign;
import net.microfalx.bootstrap.web.chart.style.VerticalAlign;

@Data
@ToString
public class NoData {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String text;
    @JsonProperty("align")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private HorizontalAlign horizontalAlign;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private VerticalAlign verticalAlign;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double offsetX;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double offsetY;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Style style;

}
