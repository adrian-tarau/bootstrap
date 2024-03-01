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
public class CounterDataSet extends PojoDataSet<Counter, PojoField<Counter>, String> {

    public CounterDataSet(DataSetFactory<Counter, PojoField<Counter>, String> factory, Metadata<Counter, PojoField<Counter>, String> metadata) {
        super(factory, metadata);
    }

    @Override
    protected Page<Counter> doFindAll(Pageable pageable, Filter filterable) {
        List<Counter> timers = Metrics.ROOT.getCounters().stream().map(Counter::from).collect(Collectors.toList());
        return getPage(timers, pageable, filterable);
    }
}
