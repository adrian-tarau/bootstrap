package net.microfalx.bootstrap.web.controller.support.pool;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.OrderBy;
import net.microfalx.lang.ClassUtils;
import net.microfalx.lang.ExceptionUtils;
import net.microfalx.lang.annotation.*;
import net.microfalx.threadpool.TaskDescriptor;
import org.springframework.data.annotation.CreatedDate;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString(callSuper = true)
@Name("Completed Tasks")
public class CompletedTask extends AbstractTask {

    @Position(30)
    @Label(value = "Started At")
    @Description("The timestamp when the task was started")
    @OrderBy(OrderBy.Direction.DESC)
    @CreatedDate
    @CreatedAt
    private LocalDateTime startedAt;

    @Position(31)
    @Label(value = "Duration")
    @Description("The execution duration")
    private Duration duration;

    @Position(50)
    @Label(value = "Failure (Class)")
    @Description("The exception class name if the task has failed")
    private String throwableClassName;

    @Position(51)
    @Visible(value = false)
    private String throwableStackTrace;

    public static CompletedTask from(TaskDescriptor taskDescriptor) {
        CompletedTask model = new CompletedTask();
        AbstractTask.update(model, taskDescriptor);
        model.setStartedAt(taskDescriptor.getStartedAt());
        model.setDuration(taskDescriptor.getDuration());
        if (taskDescriptor.getThrowable() != null) {
            model.setThrowableClassName(ClassUtils.getCompactName(taskDescriptor.getThrowable()));
            model.setThrowableStackTrace(ExceptionUtils.getStackTrace(taskDescriptor.getThrowable()));
        }
        return model;
    }
}
