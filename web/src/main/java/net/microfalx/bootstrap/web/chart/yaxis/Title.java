package net.microfalx.bootstrap.web.chart.yaxis;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Title {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String text;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Number rotate;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Number offsetX;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Number offsetY;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Style style;

    @Data
    @ToString
    public static class Style {

        private String color;
        private String fontSize;
        private String fontFamily;
        private String cssClass;
    }

}
