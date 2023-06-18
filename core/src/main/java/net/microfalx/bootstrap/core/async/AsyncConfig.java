package net.microfalx.bootstrap.core.async;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.ScheduledThreadPoolExecutor;

@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {

    @Bean
    public ThreadPoolTaskScheduler getTaskScheduler() {
        ThreadPoolTaskScheduler executor = new ThreadPoolTaskScheduler();
        executor.setThreadNamePrefix("bootstrap");
        executor.initialize();
        ScheduledThreadPoolExecutor poolExecutor = executor.getScheduledThreadPoolExecutor();
        poolExecutor.setCorePoolSize(5);
        poolExecutor.setMaximumPoolSize(10);
        return executor;
    }
}
