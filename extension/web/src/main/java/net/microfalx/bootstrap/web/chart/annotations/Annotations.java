package net.microfalx.bootstrap.web.chart.annotations;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

import java.util.Arrays;
import java.util.List;

@Data
@ToString
public class Annotations {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String position;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<YAxisAnnotations> yaxis;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<XAxisAnnotations> xaxis;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<PointAnnotations> points;

    public static Annotations zeroY() {
        return new Annotations().setYaxis(Arrays.asList(YAxisAnnotations.zero()));
    }

    public static Annotations zeroY(String fillColor) {
        return new Annotations().setYaxis(Arrays.asList(YAxisAnnotations.zero(fillColor)));
    }

}
