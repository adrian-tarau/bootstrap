package net.microfalx.bootstrap.web.chart.markers;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class Markers {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double[] size;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> colors;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String strokeColor;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double strokeWidth;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double strokeOpacity;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double fillOpacity;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private DiscretePoint[] discrete;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Shape shape;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double radius;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double offsetX;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double offsetY;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Hover hover;

    public Markers() {
    }

    public Double[] getSize() {
        return size;
    }

    public List<String> getColors() {
        return colors;
    }

    public String getStrokeColor() {
        return strokeColor;
    }

    public Double getStrokeWidth() {
        return strokeWidth;
    }

    public Double getStrokeOpacity() {
        return strokeOpacity;
    }

    public Double getFillOpacity() {
        return fillOpacity;
    }

    public DiscretePoint[] getDiscrete() {
        return discrete;
    }

    public Shape getShape() {
        return shape;
    }

    public Double getRadius() {
        return radius;
    }

    public Double getOffsetX() {
        return offsetX;
    }

    public Double getOffsetY() {
        return offsetY;
    }

    public Hover getHover() {
        return hover;
    }

    public void setSize(Double[] size) {
        this.size = size;
    }

    public void setColors(List<String> colors) {
        this.colors = colors;
    }

    public void setStrokeColor(String strokeColor) {
        this.strokeColor = strokeColor;
    }

    public void setStrokeWidth(Double strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public void setStrokeOpacity(Double strokeOpacity) {
        this.strokeOpacity = strokeOpacity;
    }

    public void setFillOpacity(Double fillOpacity) {
        this.fillOpacity = fillOpacity;
    }

    public void setDiscrete(DiscretePoint[] discrete) {
        this.discrete = discrete;
    }

    public void setShape(Shape shape) {
        this.shape = shape;
    }

    public void setRadius(Double radius) {
        this.radius = radius;
    }

    public void setOffsetX(Double offsetX) {
        this.offsetX = offsetX;
    }

    public void setOffsetY(Double offsetY) {
        this.offsetY = offsetY;
    }

    public void setHover(Hover hover) {
        this.hover = hover;
    }

}
