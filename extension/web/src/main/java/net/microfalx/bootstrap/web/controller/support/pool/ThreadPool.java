package net.microfalx.bootstrap.web.controller.support.pool;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.model.NamedIdentityAware;
import net.microfalx.lang.annotation.*;

@Getter
@Setter
@ToString
@Name("Thread Pools")
@ReadOnly
public class ThreadPool extends NamedIdentityAware<String> {

    @Position(20)
    @Label(value = "Threads", group = "Settings")
    @Description("The maximum number of threads")
    private int maximumSize;

    @Position(21)
    @Label(value = "Queue", group = "Settings")
    @Description("The maximum number of queued tasks")
    private int queueSize;

    @Position(22)
    @Label(value = "Daemon", group = "Settings")
    @Description("Indicates whether the threads supporting this pool are daemon threads")
    private boolean daemon;

    @Position(23)
    @Label(value = "Virtual", group = "Settings")
    @Description("Indicates whether the threads supporting this pool are virtual threads")
    private boolean virtual;

    @Position(30)
    @Label(value = "Available", group = "Threads")
    @Description("The number of threads available to execute tasks")
    private long availableThreads;

    @Position(31)
    @Label(value = "Created", group = "Threads")
    @Description("The number of threads created by the pool")
    private long createdThreads;

    @Position(31)
    @Label(value = "Destroyed", group = "Threads")
    @Description("The number of threads destroyed by the pool")
    private long destroyedThreads;

    @Position(40)
    @Label(value = "Running", group = "Tasks")
    @Description("The number of tasks in execution")
    private long runningTasks;

    @Position(41)
    @Label(value = "Pending", group = "Tasks")
    @Description("The number of tasks waiting for execution")
    private long pendingTasks;

    @Position(42)
    @Label(value = "Executed", group = "Tasks")
    @Description("The number of executed tasks")
    private long executedTasks;

    @Position(43)
    @Label(value = "Failed", group = "Tasks")
    @Description("The number of failed tasks")
    private long failedTasks;

    public static ThreadPool basic(net.microfalx.threadpool.ThreadPool threadPool) {
        final ThreadPool model = new ThreadPool();
        model.setId(threadPool.getId());
        model.setName(threadPool.getName());
        return model;
    }

    public static ThreadPool from(net.microfalx.threadpool.ThreadPool threadPool) {
        final net.microfalx.threadpool.ThreadPool.Options options = threadPool.getOptions();
        final net.microfalx.threadpool.ThreadPool.Metrics metrics = threadPool.getMetrics();
        final ThreadPool model = basic(threadPool);

        model.setId(threadPool.getId());
        model.setName(threadPool.getName());
        model.setMaximumSize(options.getMaximumSize());
        model.setQueueSize(options.getQueueSize());
        model.setDaemon(options.isDaemon());
        model.setVirtual(options.isVirtual());

        model.setAvailableThreads(metrics.getAvailableThreadCount());
        model.setCreatedThreads(metrics.getCreatedThreadCount());
        model.setDestroyedThreads(metrics.getDestroyedThreadCount());
        model.setExecutedTasks(metrics.getExecutedTaskCount());
        model.setRunningTasks(metrics.getRunningTaskCount());
        model.setPendingTasks(metrics.getPendingTaskCount());
        model.setFailedTasks(metrics.getFailedTaskCount());

        return model;
    }
}
