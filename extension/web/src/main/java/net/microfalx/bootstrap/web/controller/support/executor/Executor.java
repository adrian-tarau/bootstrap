package net.microfalx.bootstrap.web.controller.support.executor;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.bootstrap.dataset.model.IdentityAware;
import net.microfalx.lang.annotation.*;

@Getter
@Setter
@ToString
@Name("Executors")
@ReadOnly
public class Executor extends IdentityAware<String> {

    @Position(1)
    @Name
    @Description("The name of the executor")
    private String name;

    @Position(2)
    @Description("The type of the executor")
    private Type type;

    @Position(10)
    @Description("The number of pending tasks")
    @Label(value = "Pending", group = "Tasks")
    private int pendingTasks;

    @Position(11)
    @Description("The number of completed tasks")
    @Label(value = "Completed", group = "Tasks")
    private int completedTasks;

    @Position(20)
    @Description("The number of threads executing tasks")
    @Label(value = "Running", group = "Threads")
    private int threadsRunning;

    @Position(21)
    @Description("The number of threads waiting for a task (idle)")
    @Label(value = "Idle", group = "Threads")
    private int threadsIdle;

    @Position(22)
    @Description("The maximum number of core threads")
    @Label(value = "Core", group = "Threads")
    private int coreThreads;

    @Position(23)
    @Description("The maximum number of threads when the queue is full")
    @Label(value = "Maximum", group = "Threads")
    @Formattable(negativeValue = Formattable.NA)
    private int maxThreads;

    @Position(30)
    @Description("The size of the queue")
    @Label(value = "Size", group = "Queue")
    private int queueSize;

    @Position(31)
    @Description("The maximum size of the queue")
    @Label(value = "Capacity", group = "Queue")
    @Formattable(negativeValue = Formattable.NA)
    private int queueCapacity;

    public enum Type {
        UNKNOWN,
        EXECUTOR,
        ASYNC_EXECUTOR,
        SCHEDULER
    }
}
