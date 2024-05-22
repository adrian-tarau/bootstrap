package net.microfalx.bootstrap.web.chart.legend;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Legend {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean show;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean showForSingleSeries;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean showForNullSeries;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean showForZeroSeries;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean floating;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Position position;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private HorizontalAlign horizontalAlign;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String fontSize;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String fontFamily;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double width;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double height;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double offsetX;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double offsetY;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Markers markers;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ItemMargin itemMargin;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ContainerMargin containerMargin;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private OnItemClick onItemClick;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private OnItemHover onItemHover;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String formatter;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String textAnchor;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Labels labels;

}
