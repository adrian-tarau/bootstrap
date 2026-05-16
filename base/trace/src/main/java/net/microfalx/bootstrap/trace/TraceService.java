package net.microfalx.bootstrap.trace;

import lombok.CustomLog;
import net.microfalx.bootstrap.resource.ResourceService;
import net.microfalx.bootstrap.store.StoreService;
import net.microfalx.bootstrap.trace.startup.ApplicationStartupImpl;
import net.microfalx.bootstrap.trace.startup.StartupRecorderTask;
import net.microfalx.bootstrap.trace.startup.StartupTimeline;
import net.microfalx.threadpool.ThreadPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.metrics.ApplicationStartup;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * A service which configures and tracks the application traces.
 */
@Service
@CustomLog
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceService implements ApplicationListener<ApplicationReadyEvent> {

    // Do not remove, it is wired because resource service setups the JVM paths
    @Autowired(required = false) private ResourceService resourceService;
    @Autowired ThreadPool threadPool;
    @Autowired private ApplicationContext applicationContext;
    @Autowired private StoreService storeService;

    /**
     * Returns the startup timeline for the current instance.
     *
     * @return a non-null instance
     */
    public StartupTimeline getStartupTimeline() {
        if (applicationContext instanceof ConfigurableApplicationContext configurableApplicationContext) {
            ApplicationStartup applicationStartup = configurableApplicationContext.getApplicationStartup();
            if (applicationStartup instanceof ApplicationStartupImpl customStartup) {
                return customStartup.getTimeline();
            }
        }
        return new StartupTimeline();
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        recordMetrics(event.getTimeTaken());
    }

    private void recordMetrics(Duration duration) {
        threadPool.execute(new StartupRecorderTask(getStartupTimeline(), duration));
    }
}
