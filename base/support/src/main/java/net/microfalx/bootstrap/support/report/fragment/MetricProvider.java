package net.microfalx.bootstrap.support.report.fragment;

import net.microfalx.bootstrap.support.report.AbstractFragmentProvider;
import net.microfalx.bootstrap.support.report.Fragment;
import net.microfalx.bootstrap.support.report.Template;
import net.microfalx.metrics.Counter;
import net.microfalx.metrics.Gauge;
import net.microfalx.metrics.Metrics;
import net.microfalx.metrics.Timer;

import java.util.Comparator;

@net.microfalx.lang.annotation.Provider
public class MetricProvider extends AbstractFragmentProvider {

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
}
