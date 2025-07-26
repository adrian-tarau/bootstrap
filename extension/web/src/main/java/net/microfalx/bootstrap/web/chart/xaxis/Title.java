package net.microfalx.bootstrap.web.chart.xaxis;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Title {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String text;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double offsetX;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double offsetY;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Style style;

    @Data
    @ToString
    public static class Style {

        private String color;
        private String fontSize;
        private String cssClass;

    }

}
