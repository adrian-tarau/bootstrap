package net.microfalx.bootstrap.web.chart;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;
import net.microfalx.bootstrap.web.chart.animation.Animations;
import net.microfalx.bootstrap.web.chart.selection.Selection;
import net.microfalx.bootstrap.web.chart.style.Brush;
import net.microfalx.bootstrap.web.chart.style.DropShadow;
import net.microfalx.bootstrap.web.chart.toolbar.Toolbar;
import net.microfalx.bootstrap.web.chart.zoom.Zoom;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

@Data
@ToString
public class Options {

    private final Type type;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String foreColor;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String fontFamily;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String background;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double offsetX;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double offsetY;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String width;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String height;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private DropShadow dropShadow;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Brush brush;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String id;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String defaultLocale;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Sparkline sparkline;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean stacked;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private StackType stackType;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Toolbar toolbar;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Zoom zoom;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Selection selection;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Animations animations;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String group;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Events events;

    public static Options create(Type type) {
        return new Options(type);
    }

    Options(Type type) {
        requireNonNull(type);
        this.type = type;
    }

    public Options sparkline() {
        this.sparkline = new Sparkline(true);
        return this;
    }

    public Options setHeight(int pixels) {
        this.height = pixels + "px";
        return this;
    }

    public Options setHeight(String height) {
        this.height = height;
        return this;
    }

    public Options setWidth(int pixels) {
        this.width = pixels + "px";
        return this;
    }

    public Options setWidth(String width) {
        this.width = width;
        return this;
    }
}
