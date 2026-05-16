package net.microfalx.bootstrap.trace.startup;

import net.microfalx.lang.ExecutionAware;
import net.microfalx.lang.Nameable;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * An extension of the Spring Boot {@link org.springframework.core.metrics.StartupStep} which provides
 * the start/end time.
 */
public interface StartupStep extends Nameable, org.springframework.core.metrics.StartupStep,
        ExecutionAware<LocalDateTime> {

    Optional<String> getBeanClassName();
}
