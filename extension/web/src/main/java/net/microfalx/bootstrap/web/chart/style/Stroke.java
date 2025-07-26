package net.microfalx.bootstrap.web.chart.style;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class Stroke {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean show;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Curve curve;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LineCap lineCap;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> colors;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double width;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Double> widthArray;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Double> dashArray;

    public static Stroke width(double width) {
        return new Stroke().setWidth(width);
    }

    @JsonGetter("width")
    public Object serializeWidth() {
        if (width != null) {
            return width;
        }
        return widthArray;
    }

}
