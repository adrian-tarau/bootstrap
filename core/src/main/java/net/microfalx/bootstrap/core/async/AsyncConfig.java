package net.microfalx.bootstrap.core.async;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {

    @Autowired(required = false)
    private AsyncProperties properties = new AsyncProperties();

    @Bean
    public TaskExecutor getTaskExecutor() {
        return new TaskExecutorFactory().setProperties(properties).setSuffix("exec").createExecutor();
    }

    @Bean
    public TaskScheduler getTaskScheduler() {
        return new TaskExecutorFactory().setProperties(properties).setSuffix("sch").createScheduler();
    }
}
