package net.microfalx.bootstrap.web.controller.support.pool;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.lang.annotation.Description;
import net.microfalx.lang.annotation.Label;
import net.microfalx.lang.annotation.Name;
import net.microfalx.lang.annotation.Position;
import net.microfalx.threadpool.TaskDescriptor;

import java.time.Duration;

@Getter
@Setter
@ToString(callSuper = true)
@Name("Scheduled Tasks")
public class ScheduledTask extends AbstractTask {

    @Position(30)
    @Label(value = "Strategy", group = "Settings")
    @Description("The scheduling strategy")
    private net.microfalx.threadpool.ScheduledTask.Strategy strategy;

    @Position(31)
    @Label(value = "Delay", group = "Settings")
    @Description("The delay for the first execution")
    private Duration delay;

    @Position(32)
    @Label(value = "Interval", group = "Settings")
    @Description("The scheduling interval (applied based on the strategy)")
    private Duration interval;

    public static ScheduledTask from(TaskDescriptor taskDescriptor) {
        ScheduledTask model = new ScheduledTask();
        AbstractTask.update(model, taskDescriptor);
        model.setPeriodic(true);
        if (taskDescriptor instanceof net.microfalx.threadpool.ScheduledTask scheduledTaskTask) {
            model.setStrategy(scheduledTaskTask.getStrategy());
            model.setDelay(scheduledTaskTask.getInitialDelay());
            model.setInterval(scheduledTaskTask.getInterval());
        }
        return model;
    }
}
