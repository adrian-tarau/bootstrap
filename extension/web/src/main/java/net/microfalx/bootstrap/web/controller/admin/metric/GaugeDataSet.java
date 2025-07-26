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
public class GaugeDataSet extends PojoDataSet<Gauge, PojoField<Gauge>, String> {

    public GaugeDataSet(DataSetFactory<Gauge, PojoField<Gauge>, String> factory, Metadata<Gauge, PojoField<Gauge>, String> metadata) {
        super(factory, metadata);
    }

    @Override
    protected Page<Gauge> doFindAll(Pageable pageable, Filter filterable) {
        List<Gauge> timers = Metrics.ROOT.getGauges().stream().map(Gauge::from).collect(Collectors.toList());
        return getPage(timers, pageable, filterable);
    }
}
