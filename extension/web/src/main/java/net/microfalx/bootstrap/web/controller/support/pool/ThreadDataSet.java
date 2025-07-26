package net.microfalx.bootstrap.web.controller.support.pool;

import net.microfalx.bootstrap.dataset.DataSetFactory;
import net.microfalx.bootstrap.dataset.PojoDataSet;
import net.microfalx.bootstrap.model.Filter;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.model.PojoField;
import net.microfalx.lang.annotation.Provider;
import net.microfalx.threadpool.ThreadPoolUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Provider
public class ThreadDataSet extends PojoDataSet<Thread, PojoField<Thread>, Long> {

    public ThreadDataSet(DataSetFactory<Thread, PojoField<Thread>, Long> factory, Metadata<Thread, PojoField<Thread>, Long> metadata) {
        super(factory, metadata);
    }

    @Override
    protected Page<Thread> doFindAll(Pageable pageable, Filter filterable) {
        List<Thread> tasks = Arrays.stream(ThreadPoolUtils.getThreads())
                .map(Thread::from).collect(Collectors.toList());
        return getPage(tasks, pageable, filterable);
    }
}
