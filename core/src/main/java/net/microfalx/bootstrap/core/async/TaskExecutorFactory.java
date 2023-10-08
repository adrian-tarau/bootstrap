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
 * The settings can be customized using {@link AsynchronousProperties}.
 */
public class TaskExecutorFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskExecutorFactory.class);

    private static final AtomicInteger POOL_ID = new AtomicInteger(1);

    private AsynchronousProperties properties = new AsynchronousProperties();
    private float ratio = 1;

    public static TaskExecutorFactory create() {
        return new TaskExecutorFactory();
    }

    public static TaskExecutorFactory create(String suffix) {
        return new TaskExecutorFactory().setSuffix(suffix);
    }

    public static TaskExecutorFactory create(AsynchronousProperties properties) {
        return new TaskExecutorFactory().setProperties(properties);
    }

    TaskExecutorFactory() {
    }

    public AsynchronousProperties getProperties() {
        return properties;
    }

    public TaskExecutorFactory setProperties(AsynchronousProperties properties) {
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

    public TaskExecutorFactory setRatio(float ratio) {
        this.ratio = ratio;
        return this;
    }

    public AsyncTaskExecutor createExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();

        taskExecutor.setThreadNamePrefix(getThreadNamePrefix());
        taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        taskExecutor.setAwaitTerminationSeconds((int) properties.getAwaitTermination().getSeconds());
        taskExecutor.setCorePoolSize((int) (properties.getCoreThreads() * ratio));
        taskExecutor.setMaxPoolSize((int) (properties.getMaximumThreads() * ratio));
        taskExecutor.setQueueCapacity((int) (properties.getQueueCapacity() * ratio));
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
        poolExecutor.setCorePoolSize((int) (properties.getCoreThreads() * ratio));
        poolExecutor.setMaximumPoolSize((int) (properties.getMaximumThreads() * ratio));
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
