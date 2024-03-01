package net.microfalx.bootstrap.web.controller.admin.metric;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.lang.annotation.Description;
import net.microfalx.lang.annotation.Name;
import net.microfalx.lang.annotation.Position;

@Getter
@Setter
@ToString
@Name("Gauges")
public class Gauge extends Metric {

    @Position(10)
    @Description("The number of times the counter was incremented")
    private long value;

    public static Gauge from(net.microfalx.metrics.Gauge value) {
        Gauge gauge = new Gauge();
        update(gauge, value);
        gauge.setValue(value.getValue());
        return gauge;
    }
}
