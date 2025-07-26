package net.microfalx.bootstrap.core.async;

import net.microfalx.threadpool.ThreadPool;
import net.microfalx.threadpool.ThreadPoolUtils;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@EnableScheduling
public class AsynchronousConfig implements AsyncConfigurer, SchedulingConfigurer {

    @Autowired(required = false)
    private AsynchronousProperties properties = new AsynchronousProperties();

    @Bean
    @Primary
    public AsyncTaskExecutor getTaskExecutor() {
        return new TaskExecutorFactory().setProperties(properties).createExecutor();
    }

    @Bean
    public TaskScheduler getTaskScheduler() {
        return new TaskExecutorFactory().setProperties(properties).createScheduler();
    }

    @Bean
    public ThreadPool getThreadPool() {
        ThreadPool threadPool = ThreadPool.builder(properties.getPrefix()).maximumSize(properties.getCoreThreads())
                .queueSize(properties.getQueueCapacity()).virtual(properties.isVirtual())
                .getOrBuild();
        ThreadPoolUtils.setDefault(threadPool);
        return threadPool;
    }

    @Override
    public Executor getAsyncExecutor() {
        return getTaskExecutor();
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> TaskExecutorFactory.LOGGER.atError().setCause(ex)
                .log("Unhandled exception in method '{}'", method.toGenericString());
    }

}
