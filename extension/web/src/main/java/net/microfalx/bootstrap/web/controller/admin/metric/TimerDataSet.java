package net.microfalx.bootstrap.web.controller.admin.metric;

import net.microfalx.bootstrap.dataset.DataSetFactory;
import net.microfalx.bootstrap.dataset.PojoDataSet;
import net.microfalx.bootstrap.model.Filter;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.model.PojoField;
import net.microfalx.lang.annotation.Provider;
import net.microfalx.metrics.Metrics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;

@Provider
public class TimerDataSet extends PojoDataSet<Timer, PojoField<Timer>, String> {

    public TimerDataSet(DataSetFactory<Timer, PojoField<Timer>, String> factory, Metadata<Timer, PojoField<Timer>, String> metadata) {
        super(factory, metadata);
    }

    @Override
    protected Page<Timer> doFindAll(Pageable pageable, Filter filterable) {
        List<Timer> timers = Metrics.ROOT.getTimers().stream().map(Timer::from).collect(Collectors.toList());
        return getPage(timers, pageable, filterable);
    }


}
