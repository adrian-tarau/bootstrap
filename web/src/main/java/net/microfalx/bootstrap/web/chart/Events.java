package net.microfalx.bootstrap.web.chart;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Events {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String animationEnd;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String beforeMount;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String mounted;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String updated;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String click;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String mouseMove;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String legendClick;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String markerClick;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String selection;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String dataPointSelection;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String dataPointMouseEnter;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String dataPointMouseLeave;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String beforeZoom;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String beforeResetZoom;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String zoomed;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String scrolled;

}
