package net.microfalx.bootstrap.trace.startup;

import net.microfalx.lang.ExecutionAware;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Holds the startup timeline.
 */
public class StartupTimeline implements ExecutionAware<LocalDateTime> {

    static final AtomicInteger ID_GENERATOR = new AtomicInteger(1);

    final Collection<StartupStep> steps = new ConcurrentLinkedQueue<>();

    /**
     * Returns the steps registered during startup.
     *
     * @return a non-null instance
     */
    public Collection<StartupStep> getSteps() {
        return steps;
    }

    @Override
    public LocalDateTime getStartedAt() {
        return steps.stream().map(ExecutionAware::getStartedAt).min(Comparator.naturalOrder()).orElseThrow();
    }

    @Override
    public LocalDateTime getEndedAt() {
        return steps.stream().map(ExecutionAware::getEndedAt).max(Comparator.naturalOrder()).orElseThrow();
    }
}
