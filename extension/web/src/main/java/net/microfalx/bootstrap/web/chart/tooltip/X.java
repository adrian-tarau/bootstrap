package net.microfalx.bootstrap.web.chart.tooltip;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;
import net.microfalx.bootstrap.web.chart.Function;

@Data
@ToString
public class X {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean show;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String format;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Function formatter;

    public static X show() {
        return new X().setShow(true);
    }

    public static X hide() {
        return new X().setShow(false);
    }

    public static X timestamp() {
        X x = new X();
        x.setShow(true);
        x.setFormatter(Function.Tooltip.formatTimestamp());
        return x;
    }


}
