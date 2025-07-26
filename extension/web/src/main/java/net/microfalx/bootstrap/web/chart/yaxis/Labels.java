package net.microfalx.bootstrap.web.chart.yaxis;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Labels {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean show;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double minWidth;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double maxWidth;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double offsetX;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double offsetY;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double rotate;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Align align;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double padding;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Style style;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String formatter;

    @Data
    @ToString
    public static class Style {

        private String color;
        private String fontSize;
        private String fontFamily;
        private String cssClass;

    }

}
