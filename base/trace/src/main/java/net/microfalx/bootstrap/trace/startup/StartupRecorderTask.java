package net.microfalx.bootstrap.trace.startup;

import net.microfalx.metrics.Metrics;
import net.microfalx.metrics.Timer;
import net.microfalx.threadpool.Task;

import java.time.Duration;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

public class StartupRecorderTask implements Runnable, Task {

    private final StartupTimeline timeline;
    private final Duration duration;

    public StartupRecorderTask(StartupTimeline timeline, Duration duration) {
        requireNonNull(timeline);
        requireNonNull(duration);
        this.timeline = timeline;
        this.duration = duration;
    }

    @Override
    public void run() {
        recordMetrics();
        persistTrace();
    }

    private void recordMetrics() {
        for (StartupStep step : timeline.getSteps()) {
            STARTUP_TYPE.getTimer(step.getName(), Timer.Type.SHORT).record(step.getDuration());
            step.getBeanClassName().ifPresent(s -> {
                STARTUP_BEAN.getTimer(s, Timer.Type.SHORT).record(step.getDuration());
            });
        }
    }

    private void persistTrace() {

    }

    private static final Metrics STARTUP = Metrics.of("Spring").withGroup("Startup");
    private static final Metrics STARTUP_TYPE = STARTUP.withGroup("Type");
    private static final Metrics STARTUP_BEAN = STARTUP.withGroup("Bean");
}
