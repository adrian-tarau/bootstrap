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
public class RunningTaskDataSet extends PojoDataSet<RunningTask, PojoField<RunningTask>, Long> {

    public RunningTaskDataSet(DataSetFactory<RunningTask, PojoField<RunningTask>, Long> factory, Metadata<RunningTask, PojoField<RunningTask>, Long> metadata) {
        super(factory, metadata);
    }

    @Override
    protected Page<RunningTask> doFindAll(Pageable pageable, Filter filterable) {
        List<RunningTask> tasks = net.microfalx.threadpool.ThreadPool.list().stream()
                .flatMap(threadPool -> threadPool.getRunningTasks().stream())
                .map(RunningTask::from).collect(Collectors.toList());
        return getPage(tasks, pageable, filterable);
    }
}
