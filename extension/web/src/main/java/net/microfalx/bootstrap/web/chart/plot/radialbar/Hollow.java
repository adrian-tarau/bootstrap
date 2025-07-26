package net.microfalx.bootstrap.web.chart.plot.radialbar;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;
import net.microfalx.bootstrap.web.chart.plot.hollow.HollowPosition;
import net.microfalx.bootstrap.web.chart.style.DropShadow;

@Data
@ToString
public class Hollow {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double margin;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String size;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String background;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String image;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double width;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double height;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double offsetX;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double offsetY;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean clipped;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private HollowPosition position;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private DropShadow dropShadow;

    public Hollow() {
    }

    public Double getMargin() {
        return margin;
    }

    public String getSize() {
        return size;
    }

    public String getBackground() {
        return background;
    }

    public String getImage() {
        return image;
    }

    public Double getWidth() {
        return width;
    }

    public Double getHeight() {
        return height;
    }

    public Double getOffsetX() {
        return offsetX;
    }

    public Double getOffsetY() {
        return offsetY;
    }

    public Boolean getClipped() {
        return clipped;
    }

    public HollowPosition getPosition() {
        return position;
    }

    public void setMargin(Double margin) {
        this.margin = margin;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setWidth(Double width) {
        this.width = width;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public void setOffsetX(Double offsetX) {
        this.offsetX = offsetX;
    }

    public void setOffsetY(Double offsetY) {
        this.offsetY = offsetY;
    }

    public void setClipped(Boolean clipped) {
        this.clipped = clipped;
    }

    public void setPosition(HollowPosition position) {
        this.position = position;
    }

    public DropShadow getDropShadow() {
        return dropShadow;
    }

    public void setDropShadow(DropShadow dropShadow) {
        this.dropShadow = dropShadow;
    }
}
