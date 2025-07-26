package net.microfalx.bootstrap.web.controller.admin.metric;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Filterable;
import net.microfalx.bootstrap.dataset.annotation.OrderBy;
import net.microfalx.lang.annotation.*;

import java.time.Duration;

@Getter
@Setter
@ToString
@Name("Timers")
public class Timer extends Metric {

    @Position(4)
    @Description("The type of the timer")
    @Visible(value = false)
    @Width("90px")
    private net.microfalx.metrics.Timer.Type type;

    @Position(10)
    @Description("The number of times that stop has been called on this timer")
    @Width("90px")
    private long count;

    @Position(11)
    @Description("The total time of recorded events")
    @OrderBy(OrderBy.Direction.DESC)
    @Filterable
    @Label(value = "Total", group = "Duration")
    @Width("90px")
    private Duration duration;

    @Position(20)
    @Label(value = "Average", group = "Duration")
    @Description("The average time of recorded events")
    @Filterable
    @Width("90px")
    private Duration averageDuration;

    @Position(21)
    @Label(value = "Minimum", group = "Duration")
    @Description("The minimum time of recorded events")
    @Filterable
    @Visible(false)
    @Width("90px")
    private Duration minimumDuration;

    @Position(22)
    @Label(value = "Maximum", group = "Duration")
    @Description("The maximum time of recorded events")
    @Filterable
    @Width("90px")
    private Duration maximumDuration;

    public static Timer from(net.microfalx.metrics.Timer value) {
        Timer timer = new Timer();
        update(timer, value);
        timer.setType(value.getType());
        timer.setCount(value.getCount());
        timer.setDuration(value.getDuration());
        timer.setAverageDuration(value.getAverageDuration());
        timer.setMinimumDuration(value.getMinimumDuration());
        timer.setMaximumDuration(value.getMaximumDuration());
        return timer;
    }
}
