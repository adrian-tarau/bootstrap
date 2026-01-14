package net.microfalx.bootstrap.support.report.fragment;

import net.microfalx.bootstrap.support.report.*;
import net.microfalx.lang.ClassUtils;
import net.microfalx.lang.ExceptionUtils;
import net.microfalx.lang.annotation.Provider;
import net.microfalx.threadpool.Task;
import net.microfalx.threadpool.TaskDescriptor;
import net.microfalx.threadpool.ThreadPool;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Provider
public class TaskProvider extends AbstractFragmentProvider {

    private final TrendHelper trendHelper = new TrendHelper();

    @Override
    public Fragment create() {
        return Fragment.builder("Tasks").template("task")
                .icon("fa-solid fa-plug")
                .order(800)
                .build();
    }

    @Override
    public void update(Template template) {
        super.update(template);
        template.addVariable("taskHelper", this);
        template.addVariable("threadPools", ThreadPool.list());
        List<TaskDescriptor> runningTasks = net.microfalx.threadpool.ThreadPool.list().stream()
                .flatMap(threadPool -> threadPool.getRunningTasks().stream())
                .toList();
        template.addVariable("runningTasks", runningTasks);
        List<TaskDescriptor> completedTasks = net.microfalx.threadpool.ThreadPool.list().stream()
                .flatMap(threadPool -> threadPool.getCompletedTasks().stream())
                .toList();
        template.addVariable("completedTasks", completedTasks);
        List<TaskDescriptor> failedTasks = completedTasks.stream().filter(descriptor -> descriptor.getThrowable() != null).toList();
        template.addVariable("failedTasks", failedTasks);
        template.addVariable("failedAggregatedTasks", getFailedTasksAggregated(failedTasks));
        List<TaskDescriptor> scheduledTasks = net.microfalx.threadpool.ThreadPool.list().stream()
                .flatMap(threadPool -> threadPool.getScheduledTasks().stream())
                .toList();
        template.addVariable("scheduledTasks", scheduledTasks);
    }

    public Chart.PieChart<Integer> getFailedTaskNamePieChart(String id, Collection<FailedTasksAggregated> failedTasks) {
        Chart.PieChart<Integer> chart = new Chart.PieChart<>(id, "Name");
        chart.getLegend().setShow(false);
        trendHelper.aggregateInt(failedTasks, Task::getName,
                FailedTasksAggregated::getCount).forEach(chart::add);
        return chart;
    }

    public Chart.PieChart<Integer> getFailedTaskFailureTypePieChart(String id, Collection<FailedTasksAggregated> failedTasks) {
        Chart.PieChart<Integer> chart = new Chart.PieChart<>(id, "Failure Types");
        chart.getLegend().setShow(false);
        trendHelper.aggregateInt(failedTasks, FailedTasksAggregated::getFailureType, FailedTasksAggregated::getCount)
                .forEach(chart::add);
        return chart;
    }

    public String getThrowableClassName(TaskDescriptor descriptor) {
        return descriptor.getThrowable() != null ? ClassUtils.getCompactName(descriptor.getThrowable()) : null;
    }

    public String getThrowableStackTrace(TaskDescriptor descriptor) {
        return descriptor.getThrowable() != null ? ExceptionUtils.getStackTrace(descriptor.getThrowable()) : null;
    }

    public String getFailureType(TaskDescriptor descriptor) {
        return descriptor.getThrowable() != null ? ExceptionUtils.getRootCauseName(descriptor.getThrowable()) : null;
    }

    private Collection<FailedTasksAggregated> getFailedTasksAggregated(List<TaskDescriptor> completedTasks) {
        Map<String, FailedTasksAggregated> map = new HashMap<>();
        completedTasks.forEach(descriptor -> {
            FailedTasksAggregated aggregated = map.computeIfAbsent(descriptor.getTaskClass().getName(), id -> new FailedTasksAggregated(descriptor));
            aggregated.count++;
        });
        return map.values();
    }

    public static class FailedTasksAggregated implements TaskDescriptor {

        private final TaskDescriptor descriptor;
        private int count;

        private FailedTasksAggregated(TaskDescriptor descriptor) {
            this.descriptor = descriptor;
        }

        @Override
        public Long getId() {
            return descriptor.getId();
        }

        @Override
        public ThreadPool getThreadPool() {
            return descriptor.getThreadPool();
        }

        @Override
        public Thread getThread() {
            return descriptor.getThread();
        }

        @Override
        public Class<?> getTaskClass() {
            return descriptor.getTaskClass();
        }

        public String getFailureType() {
            return ExceptionUtils.getRootCauseName(descriptor.getThrowable());
        }

        @Override
        public LocalDateTime getStartedAt() {
            return descriptor.getStartedAt();
        }

        @Override
        public Duration getDuration() {
            return descriptor.getDuration();
        }

        @Override
        public boolean isPeriodic() {
            return descriptor.isPeriodic();
        }

        @Override
        public Throwable getThrowable() {
            return descriptor.getThrowable();
        }

        public int getCount() {
            return count;
        }
    }
}
