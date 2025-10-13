package net.microfalx.bootstrap.core.listener;

import lombok.extern.slf4j.Slf4j;
import net.microfalx.lang.ExceptionUtils;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ApplicationFailedListener implements ApplicationListener<ApplicationFailedEvent> {

    @Override
    public void onApplicationEvent(ApplicationFailedEvent event) {
        String rootCauseMessage = ExceptionUtils.getRootCauseMessage(event.getException());
        String description = "An error occurred during application initialization: " + rootCauseMessage;
        LOGGER.atError().setCause(event.getException()).log(description);
    }
}
