package net.microfalx.bootstrap.web.chart.datalabels;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;
import net.microfalx.bootstrap.web.chart.style.DropShadow;

import java.util.List;

@Data
@ToString
public class DataLabels {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean enabled;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Double> enabledOnSeries;
    private String formatter;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private TextAnchor textAnchor;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double offsetX;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double offsetY;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Style style;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private DropShadow dropShadow;

    public DataLabels() {
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public List<Double> getEnabledOnSeries() {
        return enabledOnSeries;
    }

    public String getFormatter() {
        return formatter;
    }

    public TextAnchor getTextAnchor() {
        return textAnchor;
    }

    public Double getOffsetX() {
        return offsetX;
    }

    public Double getOffsetY() {
        return offsetY;
    }

    public Style getStyle() {
        return style;
    }

    public DropShadow getDropShadow() {
        return dropShadow;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public void setEnabledOnSeries(List<Double> enabledOnSeries) {
        this.enabledOnSeries = enabledOnSeries;
    }

    public void setFormatter(String formatter) {
        this.formatter = formatter;
    }

    public void setTextAnchor(TextAnchor textAnchor) {
        this.textAnchor = textAnchor;
    }

    public void setOffsetX(Double offsetX) {
        this.offsetX = offsetX;
    }

    public void setOffsetY(Double offsetY) {
        this.offsetY = offsetY;
    }

    public void setStyle(Style style) {
        this.style = style;
    }

    public void setDropShadow(DropShadow dropShadow) {
        this.dropShadow = dropShadow;
    }

}
