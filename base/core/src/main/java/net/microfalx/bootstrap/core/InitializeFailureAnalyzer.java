package net.microfalx.bootstrap.core;

import lombok.extern.slf4j.Slf4j;
import net.microfalx.lang.ExceptionUtils;
import org.springframework.boot.diagnostics.FailureAnalysis;
import org.springframework.boot.diagnostics.FailureAnalyzer;

@Slf4j
public class InitializeFailureAnalyzer implements FailureAnalyzer {

    @Override
    public FailureAnalysis analyze(Throwable throwable) {
        String rootCauseMessage = ExceptionUtils.getRootCauseMessage(throwable);
        String description = "An error occurred during application initialization: " + rootCauseMessage;
        LOGGER.atError().setCause(throwable).log();
        return new FailureAnalysis(description, "None", throwable);
    }
}
