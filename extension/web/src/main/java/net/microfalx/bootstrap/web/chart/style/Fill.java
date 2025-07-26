package net.microfalx.bootstrap.web.chart.style;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class Fill {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> colors;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double[] opacity;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String[] type;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Gradient gradient;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Image[] image;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Pattern[] pattern;

    public Fill() {
    }

    public List<String> getColors() {
        return colors;
    }

    public Double[] getOpacity() {
        return opacity;
    }

    public String[] getType() {
        return type;
    }

    public Gradient getGradient() {
        return gradient;
    }

    public Image[] getImage() {
        return image;
    }

    public Pattern[] getPattern() {
        return pattern;
    }

    public void setColors(List<String> colors) {
        this.colors = colors;
    }

    public void setOpacity(Double... opacity) {
        this.opacity = opacity;
    }

    public void setType(String... type) {
        this.type = type;
    }

    public void setGradient(Gradient gradient) {
        this.gradient = gradient;
    }

    public void setImage(Image... image) {
        this.image = image;
    }

    public void setPattern(Pattern... pattern) {
        this.pattern = pattern;
    }

}
