package net.microfalx.bootstrap.web.chart.title;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Title {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String text;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Align align;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double margin;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double offsetX;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double offsetY;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double floating;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Style style;

}