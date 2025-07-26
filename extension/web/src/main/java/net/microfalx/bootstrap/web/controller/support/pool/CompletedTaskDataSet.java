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
public class CompletedTaskDataSet extends PojoDataSet<CompletedTask, PojoField<CompletedTask>, Long> {

    public CompletedTaskDataSet(DataSetFactory<CompletedTask, PojoField<CompletedTask>, Long> factory, Metadata<CompletedTask, PojoField<CompletedTask>, Long> metadata) {
        super(factory, metadata);
    }

    @Override
    protected Page<CompletedTask> doFindAll(Pageable pageable, Filter filterable) {
        List<CompletedTask> tasks = net.microfalx.threadpool.ThreadPool.list().stream()
                .flatMap(threadPool -> threadPool.getCompletedTasks().stream())
                .map(CompletedTask::from).collect(Collectors.toList());
        return getPage(tasks, pageable, filterable);
    }
}
