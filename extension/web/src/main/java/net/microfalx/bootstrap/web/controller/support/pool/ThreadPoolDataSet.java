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
public class ThreadPoolDataSet extends PojoDataSet<ThreadPool, PojoField<ThreadPool>, String> {

    public ThreadPoolDataSet(DataSetFactory<ThreadPool, PojoField<ThreadPool>, String> factory, Metadata<ThreadPool, PojoField<ThreadPool>, String> metadata) {
        super(factory, metadata);
    }

    @Override
    protected Page<ThreadPool> doFindAll(Pageable pageable, Filter filterable) {
        List<ThreadPool> threadPools = net.microfalx.threadpool.ThreadPool.list().stream().map(ThreadPool::from).collect(Collectors.toList());
        return getPage(threadPools, pageable, filterable);
    }
}
