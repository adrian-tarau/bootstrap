package net.microfalx.bootstrap.support.metric;

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
public class SummaryDataSet extends PojoDataSet<Summary, PojoField<Summary>, String> {

    public SummaryDataSet(DataSetFactory<Summary, PojoField<Summary>, String> factory, Metadata<Summary, PojoField<Summary>, String> metadata) {
        super(factory, metadata);
    }

    @Override
    protected Page<Summary> doFindAll(Pageable pageable, Filter filterable) {
        List<Summary> timers = Metrics.ROOT.getSummaries().stream().map(Summary::from).collect(Collectors.toList());
        return getPage(timers, pageable, filterable);
    }
}
