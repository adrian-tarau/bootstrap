package net.microfalx.bootstrap.support.report.fragment;

import net.microfalx.bootstrap.support.report.AbstractFragmentProvider;
import net.microfalx.bootstrap.support.report.Fragment;
import net.microfalx.bootstrap.support.report.Template;
import net.microfalx.metrics.Counter;
import net.microfalx.metrics.Gauge;
import net.microfalx.metrics.Metrics;
import net.microfalx.metrics.Timer;

import java.time.Duration;
import java.util.Comparator;

import static net.microfalx.lang.FormatterUtils.formatDuration;

@net.microfalx.lang.annotation.Provider
public class MetricProvider extends AbstractFragmentProvider {

    private static final Duration[] ZERO = new Duration[]{Duration.ZERO, Duration.ZERO, Duration.ZERO};
    private static final ThreadLocal<Duration[]> PERCENTILES = ThreadLocal.withInitial(() -> ZERO);

    @Override
    public Fragment create() {
        return Fragment.builder("Meters").template("meter")
                .icon("fa-solid fa-gauge")
                .order(700)
                .build();
    }

    @Override
    public void update(Template template) {
        super.update(template);
        Metrics root = Metrics.ROOT;
        template.addVariable("timers", root.getTimers().stream().sorted(Comparator.comparing(Timer::getAverageDuration).reversed()).toList());
        template.addVariable("counters", root.getCounters().stream().sorted(Comparator.comparing(Counter::getValue).reversed()).toList());
        template.addVariable("gauges", root.getGauges().stream().sorted(Comparator.comparing(Gauge::getValue).reversed()).toList());
        template.addVariable("meter", this);
    }

    public String getPercentile(Timer timer, int position, boolean calculate) {
        if (calculate) PERCENTILES.set(timer.getPercentiles());
        Duration duration = PERCENTILES.get()[position];
        if (duration.isZero()) {
            return "-";
        } else {
            return formatDuration(duration);
        }
    }
}
