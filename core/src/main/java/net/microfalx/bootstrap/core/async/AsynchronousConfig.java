package net.microfalx.bootstrap.core.async;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableAsync
@EnableScheduling
public class AsynchronousConfig {

    @Autowired(required = false)
    private AsynchronousProperties properties = new AsynchronousProperties();

    @Bean
    public TaskScheduler getTaskScheduler() {
        return new TaskExecutorFactory().setProperties(properties).createScheduler();
    }
}
