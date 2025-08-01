package net.microfalx.bootstrap.web.controller.admin.metric;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.OrderBy;
import net.microfalx.lang.annotation.Description;
import net.microfalx.lang.annotation.Name;
import net.microfalx.lang.annotation.Position;
import net.microfalx.lang.annotation.Width;

@Getter
@Setter
@ToString
@Name("Counters")
public class Counter extends Metric {

    @Position(10)
    @Description("The number of times the counter was incremented")
    @OrderBy(OrderBy.Direction.DESC)
    @Width("90px")
    private long value;

    public static Counter from(net.microfalx.metrics.Counter value) {
        Counter counter = new Counter();
        update(counter, value);
        counter.setValue(value.getValue());
        return counter;
    }
}
