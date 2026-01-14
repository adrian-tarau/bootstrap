package net.microfalx.bootstrap.support.report.fragment;

import net.microfalx.bootstrap.support.report.AbstractFragmentProvider;
import net.microfalx.bootstrap.support.report.Fragment;
import net.microfalx.bootstrap.support.report.Template;
import net.microfalx.lang.annotation.Provider;
import net.microfalx.threadpool.TaskDescriptor;
import net.microfalx.threadpool.ThreadPool;

import java.util.List;

@Provider
public class TaskProvider extends AbstractFragmentProvider {

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
        template.addVariable("threadPools", ThreadPool.list());
        List<TaskDescriptor> runningTasks = net.microfalx.threadpool.ThreadPool.list().stream()
                .flatMap(threadPool -> threadPool.getRunningTasks().stream())
                .toList();
        template.addVariable("runningTasks", runningTasks);
        List<TaskDescriptor> scheduledTasks = net.microfalx.threadpool.ThreadPool.list().stream()
                .flatMap(threadPool -> threadPool.getScheduledTasks().stream())
                .toList();
        template.addVariable("scheduledTasks", runningTasks);
    }
}
