package net.microfalx.bootstrap.web.controller.support.pool;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Filterable;
import net.microfalx.lang.annotation.Description;
import net.microfalx.lang.annotation.Label;
import net.microfalx.lang.annotation.Name;
import net.microfalx.lang.annotation.Position;
import net.microfalx.threadpool.TaskDescriptor;

@Getter
@Setter
@ToString(callSuper = true)
@Name("Running Tasks")
public class RunningTask extends AbstractTask {

    @Position(30)
    @Label(value = "Virtual", group = "Thread")
    @Description("Indicates whether the thread executing this task is a virtual thread")
    private boolean virtual;

    @Position(31)
    @Label(value = "State", group = "Thread")
    @Description("The state of the thread")
    private java.lang.Thread.State state;

    @Position(32)
    @Label(value = "Name", group = "Thread")
    @Description("The name of the thread executing the task. If a virtual thread and the carrier/native thread is attached, it will be displayed in squared brackets")
    @Filterable
    private String threadName;

    @Position(33)
    @Label(value = "Call Stack", group = "Thread")
    @Description("The top of the call stack (what is executed right now)")
    @Filterable
    private String executing;

    public static RunningTask from(TaskDescriptor taskDescriptor) {
        RunningTask model = new RunningTask();
        AbstractTask.update(model, taskDescriptor);
        java.lang.Thread thread = taskDescriptor.getThread();
        model.setState(java.lang.Thread.State.WAITING);
        if (thread != null) {
            model.setVirtual(thread.isVirtual());
            model.setState(thread.getState());
            model.setThreadName(net.microfalx.bootstrap.web.controller.support.pool.Thread.getThreadName(thread));
            model.setExecuting(Thread.getTopStack(thread));
        }
        return model;
    }
}
