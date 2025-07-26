package net.microfalx.bootstrap.web.controller.support.executor;

import net.microfalx.bootstrap.core.async.TaskExecutorFactory;
import net.microfalx.bootstrap.dataset.DataSetFactory;
import net.microfalx.bootstrap.dataset.PojoDataSet;
import net.microfalx.bootstrap.model.Filter;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.model.PojoField;
import net.microfalx.lang.StringUtils;
import net.microfalx.lang.annotation.Provider;
import org.joor.Reflect;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ExecutorConfigurationSupport;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

@Provider
public class ExecutorDataSet extends PojoDataSet<Executor, PojoField<Executor>, String> {

    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(1);

    public ExecutorDataSet(DataSetFactory<Executor, PojoField<Executor>, String> factory, Metadata<Executor, PojoField<Executor>, String> metadata) {
        super(factory, metadata);
    }

    @Override
    protected Optional<Executor> doFindById(String id) {
        try {
            return Optional.ofNullable(null);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    @Override
    protected Page<Executor> doFindAll(Pageable pageable, Filter filterable) {
        List<Executor> models = new ArrayList<>();
        for (TaskExecutor taskExecutor : TaskExecutorFactory.getTaskExecutors()) {
            models.add(create(taskExecutor));
        }
        for (TaskScheduler taskScheduler : TaskExecutorFactory.getTaskSchedulers()) {
            models.add(create(taskScheduler));
        }
        return getPage(models, pageable, filterable);
    }

    private Executor create(Object value) {
        Executor executor = new Executor();
        executor.setId(Integer.toString(ID_GENERATOR.getAndIncrement()));
        executor.setName("N/A");
        updateType(executor, value);
        if (value instanceof ThreadPoolTaskExecutor) {
            update(executor, (ThreadPoolTaskExecutor) value);
        } else if (value instanceof ThreadPoolTaskScheduler) {
            update(executor, (ThreadPoolTaskScheduler) value);
        }
        return executor;
    }

    private void updateType(Executor executor, Object value) {
        if (value instanceof AsyncTaskExecutor) {
            executor.setType(Executor.Type.ASYNC_EXECUTOR);
        } else if (value instanceof TaskExecutor) {
            executor.setType(Executor.Type.EXECUTOR);
        } else if (value instanceof TaskScheduler) {
            executor.setType(Executor.Type.SCHEDULER);
        } else {
            executor.setType(Executor.Type.UNKNOWN);
        }
    }

    private void update(Executor executor, ThreadPoolTaskExecutor threadPoolTaskExecutor) {
        update(executor, (ExecutorConfigurationSupport) threadPoolTaskExecutor);
        executor.setName(StringUtils.capitalizeWords(threadPoolTaskExecutor.getThreadNamePrefix()));
        executor.setCoreThreads(threadPoolTaskExecutor.getCorePoolSize());
        executor.setMaxThreads(adjustThreadCount(threadPoolTaskExecutor.getMaxPoolSize()));
        executor.setThreadsRunning(threadPoolTaskExecutor.getActiveCount());
        executor.setQueueCapacity(threadPoolTaskExecutor.getQueueCapacity());
        executor.setQueueSize(threadPoolTaskExecutor.getQueueSize());
        executor.setPendingTasks(threadPoolTaskExecutor.getActiveCount() + threadPoolTaskExecutor.getQueueSize());
        executor.setCompletedTasks((int) threadPoolTaskExecutor.getThreadPoolExecutor().getCompletedTaskCount());
    }

    private void update(Executor executor, ThreadPoolTaskScheduler threadPoolTaskScheduler) {
        update(executor, (ExecutorConfigurationSupport) threadPoolTaskScheduler);
        executor.setName(StringUtils.capitalizeWords(threadPoolTaskScheduler.getThreadNamePrefix()));
        executor.setCoreThreads(threadPoolTaskScheduler.getScheduledThreadPoolExecutor().getCorePoolSize());
        executor.setMaxThreads(adjustThreadCount(threadPoolTaskScheduler.getScheduledThreadPoolExecutor().getMaximumPoolSize()));
        executor.setThreadsRunning(threadPoolTaskScheduler.getActiveCount());
        executor.setQueueCapacity(getCapacity(threadPoolTaskScheduler.getScheduledThreadPoolExecutor().getQueue()));
        executor.setQueueSize(threadPoolTaskScheduler.getScheduledThreadPoolExecutor().getQueue().size());
        executor.setPendingTasks(threadPoolTaskScheduler.getActiveCount() + threadPoolTaskScheduler.getScheduledThreadPoolExecutor().getQueue().size());
        executor.setCompletedTasks((int) threadPoolTaskScheduler.getScheduledThreadPoolExecutor().getCompletedTaskCount());
    }

    private int adjustThreadCount(int threadCount) {
        return threadCount == Integer.MAX_VALUE ? -1 : threadCount;
    }

    private void update(Executor executor, ExecutorConfigurationSupport executorConfigurationSupport) {
        String beanName = Reflect.on(executorConfigurationSupport).get("beanName");
        executor.setId(StringUtils.toIdentifier(beanName));
        executor.setName(StringUtils.capitalizeWords(beanName));
    }

    private int getCapacity(BlockingQueue<?> blockingQueue) {
        if (blockingQueue instanceof ArrayBlockingQueue<?> arrayBlockingQueue) {
            return arrayBlockingQueue.size() + arrayBlockingQueue.remainingCapacity();
        } else if (blockingQueue instanceof LinkedBlockingQueue<?> linkedBlockingQueue) {
            return linkedBlockingQueue.size() + linkedBlockingQueue.remainingCapacity();
        } else {
            return -1;
        }
    }
}
