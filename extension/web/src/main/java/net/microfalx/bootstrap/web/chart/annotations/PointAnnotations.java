package net.microfalx.bootstrap.web.chart.annotations;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;
import net.microfalx.bootstrap.web.chart.style.Marker;

@Data
@ToString
public class PointAnnotations {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double x;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double y;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double yAxisIndex;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double seriesIndex;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Marker marker;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private AnnotationLabel label;

    public PointAnnotations() {
    }

    public Double getyAxisIndex() {
        return yAxisIndex;
    }

    public void setyAxisIndex(Double yAxisIndex) {
        this.yAxisIndex = yAxisIndex;
    }


    public Double getX() {
        return x;
    }

    public Double getY() {
        return y;
    }

    public Double getYAxisIndex() {
        return yAxisIndex;
    }

    public Double getSeriesIndex() {
        return seriesIndex;
    }

    public Marker getMarker() {
        return marker;
    }

    public AnnotationLabel getLabel() {
        return label;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public void setYAxisIndex(Double yAxisIndex) {
        this.yAxisIndex = yAxisIndex;
    }

    public void setSeriesIndex(Double seriesIndex) {
        this.seriesIndex = seriesIndex;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public void setLabel(AnnotationLabel label) {
        this.label = label;
    }

}
