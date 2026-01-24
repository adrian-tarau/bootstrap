package net.microfalx.bootstrap.web.controller.support.pool;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.lang.annotation.Description;
import net.microfalx.lang.annotation.Label;
import net.microfalx.lang.annotation.Name;
import net.microfalx.lang.annotation.Position;
import net.microfalx.threadpool.TaskDescriptor;

import java.time.Duration;
import java.time.LocalDateTime;

import static net.microfalx.lang.NumberUtils.toNumber;

@Getter
@Setter
@ToString(callSuper = true)
@Name("Scheduled Tasks")
public class ScheduledTask extends AbstractTask {

    @Position(21)
    @Label(value = "Periodic")
    @Description("Indicates whether the task is a periodic task (executed on a schedule)")
    private boolean periodic;

    @Position(30)
    @Label(value = "Strategy", group = "Settings")
    @Description("The scheduling strategy")
    private net.microfalx.threadpool.ScheduledTask.Strategy strategy;

    @Position(31)
    @Label(value = "Delay", group = "Settings")
    @Description("The delay for the first execution")
    @Formattable(unit = Formattable.Unit.MILLI_SECOND, zeroValue = Formattable.NA)
    private Duration delay;

    @Position(32)
    @Label(value = "Interval", group = "Settings")
    @Description("The scheduling interval (applied based on the strategy)")
    private Duration interval;

    @Position(40)
    @Label(value = "Count", group = "Scheduler")
    @Description("The number of times the task has been scheduled")
    @Formattable(unit = Formattable.Unit.COUNT)
    private int executionCount;

    @Position(41)
    @Label(value = "Last", group = "Scheduler")
    @Description("When was the task last scheduled to execute")
    private LocalDateTime lastExecutionTime;

    @Position(42)
    @Label(value = "Next", group = "Scheduler")
    @Description("When is the task scheduled to execute next")
    private LocalDateTime nextExecutionTime;

    public static ScheduledTask from(TaskDescriptor taskDescriptor) {
        ScheduledTask model = new ScheduledTask();
        AbstractTask.update(model, taskDescriptor);
        model.setPeriodic(taskDescriptor.isPeriodic());
        if (taskDescriptor instanceof net.microfalx.threadpool.ScheduledTask scheduledTaskTask) {
            model.setStrategy(scheduledTaskTask.getStrategy());
            model.setDelay(scheduledTaskTask.getInitialDelay());
            model.setInterval(scheduledTaskTask.getInterval());

            model.setExecutionCount(toNumber(taskDescriptor.getExecutionCount(), 0).intValue());
            model.setLastExecutionTime(taskDescriptor.getLastExecutionTime());
            model.setNextExecutionTime(taskDescriptor.getNextExecutionTime());
        }
        return model;
    }
}
