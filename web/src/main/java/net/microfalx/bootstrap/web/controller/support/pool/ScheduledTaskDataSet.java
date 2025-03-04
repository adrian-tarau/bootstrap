package net.microfalx.bootstrap.web.controller.support.pool;

import net.microfalx.bootstrap.dataset.DataSetFactory;
import net.microfalx.bootstrap.dataset.PojoDataSet;
import net.microfalx.bootstrap.model.Filter;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.model.PojoField;
import net.microfalx.lang.annotation.Provider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;

@Provider
public class ScheduledTaskDataSet extends PojoDataSet<ScheduledTask, PojoField<ScheduledTask>, Long> {

    public ScheduledTaskDataSet(DataSetFactory<ScheduledTask, PojoField<ScheduledTask>, Long> factory, Metadata<ScheduledTask, PojoField<ScheduledTask>, Long> metadata) {
        super(factory, metadata);
    }

    @Override
    protected Page<ScheduledTask> doFindAll(Pageable pageable, Filter filterable) {
        List<ScheduledTask> tasks = net.microfalx.threadpool.ThreadPool.list().stream()
                .flatMap(threadPool -> threadPool.getScheduledTasks().stream())
                .map(ScheduledTask::from).collect(Collectors.toList());
        return getPage(tasks, pageable, filterable);
    }
}
