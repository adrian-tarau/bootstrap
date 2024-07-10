package net.microfalx.bootstrap.web.chart.tooltip;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;
import net.microfalx.bootstrap.web.chart.Function;

@Data
@ToString
public class Y {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Function formatter;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Title title;

    public static Y noTitle() {
        Y y = new Y();
        y.setTitle(Title.noTitle());
        return y;
    }

    public static Y duration() {
        Y y = new Y();
        y.setFormatter(Function.Tooltip.formatDuration());
        return y;
    }

    public static Y durationNoTitle() {
        Y y = noTitle();
        y.setFormatter(Function.Tooltip.formatDuration());
        return y;
    }

}
