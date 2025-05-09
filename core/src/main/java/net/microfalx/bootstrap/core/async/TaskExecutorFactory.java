package net.microfalx.bootstrap.core.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.ErrorHandler;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A factory which builds a {@link ThreadPoolTaskScheduler} with the most common settings.
 * <p>
 * The settings can be customized using {@link AsynchronousProperties}.
 */
public class TaskExecutorFactory implements Thread.UncaughtExceptionHandler, ErrorHandler {

    static final Logger LOGGER = LoggerFactory.getLogger(TaskExecutorFactory.class);

    private static final boolean USE_POOLS = Boolean.getBoolean("bootstrap.async.use_pools");
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(1);
    private static final Collection<WeakReference<TaskExecutor>> TASK_EXECUTORS = new CopyOnWriteArrayList<>();
    private static final Collection<WeakReference<TaskScheduler>> TASK_SCHEDULERS = new CopyOnWriteArrayList<>();

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
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        LOGGER.atError().setCause(e).log("Unhandled exception in thread '{}'", t.getName());
    }

    @Override
    public void handleError(Throwable t) {
        LOGGER.atError().setCause(t).log("Unhandled exception in thread '{}'", Thread.currentThread().getName());
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
        taskExecutor.setBeanName(properties.getThreadNamePrefix() + "_" + ID_GENERATOR.getAndIncrement());
        taskExecutor.setThreadNamePrefix(properties.getThreadNamePrefix());
        taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        taskExecutor.setAwaitTerminationSeconds((int) properties.getAwaitTermination().getSeconds());
        taskExecutor.setCorePoolSize((int) (properties.getCoreThreads() * ratio));
        taskExecutor.setMaxPoolSize((int) (properties.getMaximumThreads() * ratio));
        taskExecutor.setQueueCapacity((int) (properties.getQueueCapacity() * ratio));
        taskExecutor.setVirtualThreads(properties.isVirtual());
        taskExecutor.initialize();
        LOGGER.info("Create task executor, prefix '{}', core threads {}, queue capacity {}", taskExecutor.getThreadNamePrefix(),
                taskExecutor.getCorePoolSize(), taskExecutor.getQueueCapacity());
        TASK_EXECUTORS.add(new WeakReference<>(taskExecutor));
        return taskExecutor;
    }

    public TaskScheduler createScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setBeanName(properties.getThreadNamePrefix() + "_" + ID_GENERATOR.getAndIncrement());
        taskScheduler.setThreadNamePrefix(properties.getThreadNamePrefix());
        taskScheduler.setWaitForTasksToCompleteOnShutdown(true);
        taskScheduler.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        taskScheduler.setAwaitTerminationSeconds((int) properties.getAwaitTermination().getSeconds());
        taskScheduler.setPoolSize(properties.getCoreThreads());
        taskScheduler.setRemoveOnCancelPolicy(properties.isRemoveOnCancel());
        taskScheduler.setVirtualThreads(properties.isVirtual());
        taskScheduler.initialize();
        taskScheduler.setErrorHandler(this);
        ScheduledThreadPoolExecutor poolExecutor = taskScheduler.getScheduledThreadPoolExecutor();
        poolExecutor.setCorePoolSize((int) (properties.getCoreThreads() * ratio));
        poolExecutor.setMaximumPoolSize((int) (properties.getMaximumThreads() * ratio));
        poolExecutor.setKeepAliveTime(properties.getKeepAlive().toMillis(), TimeUnit.MILLISECONDS);
        LOGGER.info("Create task scheduler, prefix '{}', core threads {}", taskScheduler.getThreadNamePrefix(),
                poolExecutor.getCorePoolSize());
        TASK_SCHEDULERS.add(new WeakReference<>(taskScheduler));
        return taskScheduler;
    }

    public static Collection<TaskScheduler> getTaskSchedulers() {
        return TASK_SCHEDULERS.stream().map(Reference::get).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static Collection<TaskExecutor> getTaskExecutors() {
        return TASK_EXECUTORS.stream().map(Reference::get).filter(Objects::nonNull).collect(Collectors.toList());
    }

}
