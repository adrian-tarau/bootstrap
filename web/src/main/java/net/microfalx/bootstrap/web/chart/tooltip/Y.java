package net.microfalx.bootstrap.web.chart.tooltip;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;
import net.microfalx.bootstrap.web.chart.Function;

@Data
@ToString
public class Y {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String formatter;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Title title;

    public static Y noTitle() {
        Y y = new Y();
        y.setTitle(new Title(Function.name("Chart.Tooltip.formatNoTitle")));
        return y;
    }

}
