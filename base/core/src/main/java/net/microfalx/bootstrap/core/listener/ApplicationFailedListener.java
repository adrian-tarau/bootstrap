package net.microfalx.bootstrap.core.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import static net.microfalx.lang.ExceptionUtils.getRootCauseDescription;

@Slf4j
@Component
public class ApplicationFailedListener implements ApplicationListener<ApplicationFailedEvent> {

    @Override
    public void onApplicationEvent(ApplicationFailedEvent event) {
        String description = "An error occurred during application initialization: " + getRootCauseDescription(event.getException());
        LOGGER.atError().setCause(event.getException()).log(description);
    }
}
