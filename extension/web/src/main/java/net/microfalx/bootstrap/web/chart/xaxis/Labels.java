package net.microfalx.bootstrap.web.chart.xaxis;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class Labels {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean show;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double rotate;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean rotateAlways;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean hideOverlappingLabels;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean showDuplicates;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean trim;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double minHeight;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double maxHeight;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Style style;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double offsetX;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double offsetY;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String format;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String formatter;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private DatetimeFormatter datetimeFormatter;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean datetimeUTC;

    @Data
    @ToString
    public class Style {

        private List<String> colors;
        private String fontSize;
        private String fontFamily;
        private String cssClass;

    }

}
