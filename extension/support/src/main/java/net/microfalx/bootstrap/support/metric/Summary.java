package net.microfalx.bootstrap.support.metric;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Filterable;
import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.bootstrap.dataset.annotation.OrderBy;
import net.microfalx.lang.annotation.*;

import java.time.Duration;

@Getter
@Setter
@ToString
@Name("Summaries")
public class Summary extends Metric {

    @Position(11)
    @Description("The total time of recorded events")
    @OrderBy(OrderBy.Direction.DESC)
    @Filterable
    @Label(value = "Total", group = "Duration")
    @Width("80px")
    private Duration duration;

    @Position(20)
    @Label(value = "Avg.", group = "Duration")
    @Description("The average time of recorded events")
    @Filterable
    @Width("80px")
    private Duration averageDuration;

    @Position(21)
    @Label(value = "Min.", group = "Duration")
    @Description("The minimum time of recorded events")
    @Filterable
    @Visible(false)
    @Width("80px")
    private Duration minimumDuration;

    @Position(22)
    @Label(value = "Max.", group = "Duration")
    @Description("The maximum time of recorded events")
    @Filterable
    @Width("80px")
    private Duration maximumDuration;

    @Position(30)
    @Label(value = "P50", group = "Percentiles")
    @Description("The P50 percentile time of recorded events")
    @Filterable
    @Width("70px")
    @Formattable(zeroValue = Formattable.NA)
    private Duration p50;

    @Position(31)
    @Label(value = "P95", group = "Percentiles")
    @Description("The P95 percentile time of recorded events")
    @Filterable
    @Width("70px")
    @Formattable(zeroValue = Formattable.NA)
    private Duration p95;

    @Position(32)
    @Label(value = "P99", group = "Percentiles")
    @Description("The P99 percentile time of recorded events")
    @Filterable
    @Width("70px")
    @Formattable(zeroValue = Formattable.NA)
    private Duration p99;

    @Position(40)
    @Description("The number of times that stop has been called on this summary")
    @Width(value = "70px", max = "90px")
    private long count;

    public static Summary from(net.microfalx.metrics.Summary value) {
        Summary timer = new Summary();
        update(timer, value);
        timer.setCount(value.getCount());
        timer.setDuration(value.getDuration());
        timer.setAverageDuration(value.getAverageDuration());
        timer.setMinimumDuration(value.getMinimumDuration());
        timer.setMaximumDuration(value.getMaximumDuration());
        Duration[] percentiles = value.getPercentiles();
        timer.setP50(percentiles[0]);
        timer.setP95(percentiles[1]);
        timer.setP99(percentiles[2]);
        return timer;
    }
}
