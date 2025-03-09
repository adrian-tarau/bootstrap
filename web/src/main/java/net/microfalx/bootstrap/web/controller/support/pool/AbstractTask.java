package net.microfalx.bootstrap.web.controller.support.pool;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.model.NamedIdentityAware;
import net.microfalx.lang.ClassUtils;
import net.microfalx.lang.annotation.*;
import net.microfalx.threadpool.TaskDescriptor;

@Getter
@Setter
@ToString
@ReadOnly
@Visible(value = false, fieldNames = "description")
public abstract class AbstractTask extends NamedIdentityAware<Long> {

    @Position(1)
    @Label(value = "Thread Pool")
    @Description("The thread pool which owns this task")
    private ThreadPool threadPool;

    @Position(20)
    @Label(value = "Class Name")
    @Description("The name of the class")
    private String className;

    public static void update(AbstractTask model, TaskDescriptor taskDescriptor) {
        model.setId(taskDescriptor.getId());
        model.setName(taskDescriptor.getName());
        model.setDescription(taskDescriptor.getDescription());
        model.setThreadPool(ThreadPool.basic(taskDescriptor.getThreadPool()));
        model.setClassName(ClassUtils.getCompactName(taskDescriptor.getTaskClass()));
    }


}
