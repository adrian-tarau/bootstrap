package net.microfalx.bootstrap.core.async;

import net.microfalx.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A factory which builds a {@link ThreadPoolTaskScheduler} with the most common settings.
 * <p>
 * The settings can be customized using {@link AsyncProperties}.
 */
public class TaskExecutorFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskExecutorFactory.class);

    private static final AtomicInteger POOL_ID = new AtomicInteger(1);

    private AsyncProperties properties = new AsyncProperties();

    public AsyncProperties getProperties() {
        return properties;
    }

    public TaskExecutorFactory setProperties(AsyncProperties properties) {
        requireNonNull(properties);
        this.properties = properties;
        return this;
    }

    public TaskExecutorFactory setSuffix(String suffix) {
        requireNonNull(suffix);
        properties.setSuffix(suffix);
        return this;
    }

    public TaskExecutorFactory setQueueCapacity(int queueCapacity) {
        properties.setQueueCapacity(queueCapacity);
        return this;
    }

    public AsyncTaskExecutor createExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();

        taskExecutor.setThreadNamePrefix(getThreadNamePrefix());
        taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        taskExecutor.setAwaitTerminationSeconds((int) properties.getAwaitTermination().getSeconds());
        taskExecutor.setCorePoolSize(properties.getCoreThreads());
        taskExecutor.setMaxPoolSize(properties.getMaximumThreads());
        taskExecutor.setQueueCapacity(properties.getQueueCapacity());
        taskExecutor.initialize();
        LOGGER.info("Create task executor, prefix '{}', core threads {}, queue capacity {}", taskExecutor.getThreadNamePrefix(),
                taskExecutor.getCorePoolSize(), taskExecutor.getQueueCapacity());

        return taskExecutor;
    }

    public TaskScheduler createScheduler() {
        ThreadPoolTaskScheduler taskExecutor = new ThreadPoolTaskScheduler();

        taskExecutor.setThreadNamePrefix(getThreadNamePrefix());
        taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        taskExecutor.setAwaitTerminationSeconds((int) properties.getAwaitTermination().getSeconds());
        taskExecutor.setPoolSize(properties.getCoreThreads());
        taskExecutor.setRemoveOnCancelPolicy(properties.isRemoveOnCancel());
        taskExecutor.initialize();

        ScheduledThreadPoolExecutor poolExecutor = taskExecutor.getScheduledThreadPoolExecutor();
        poolExecutor.setCorePoolSize(properties.getCoreThreads());
        poolExecutor.setMaximumPoolSize(properties.getMaximumThreads());
        poolExecutor.setKeepAliveTime(properties.getKeepAlive().toMillis(), TimeUnit.MILLISECONDS);

        LOGGER.info("Create task scheduler, prefix '{}', core threads {}", taskExecutor.getThreadNamePrefix(),
                poolExecutor.getCorePoolSize());
        return taskExecutor;
    }

    private String getThreadNamePrefix() {
        String name = properties.getPrefix();
        String suffix = properties.getSuffix();
        if (StringUtils.isNotEmpty(suffix)) name += "-" + suffix;
        return name;
    }
}
