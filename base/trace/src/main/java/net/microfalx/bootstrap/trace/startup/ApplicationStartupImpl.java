package net.microfalx.bootstrap.trace.startup;

import org.springframework.core.metrics.ApplicationStartup;
import org.springframework.core.metrics.StartupStep;

/**
 * An implementation for {@link ApplicationStartup} which tracks the startup events on a timeline and
 * also records these events as metrics.
 */
public class ApplicationStartupImpl implements ApplicationStartup {

    private final StartupTimeline timeline = new StartupTimeline();

    public StartupTimeline getTimeline() {
        return timeline;
    }

    @Override
    public StartupStep start(String name) {
        StartupStepImpl step = new StartupStepImpl(name);
        timeline.steps.add(step);
        return step;
    }
}
