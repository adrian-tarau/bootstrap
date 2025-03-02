package net.microfalx.bootstrap.core;

import net.microfalx.lang.FileUtils;
import net.microfalx.lang.JvmUtils;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;

public class InitializeListener implements ApplicationListener<ApplicationEvent>, Ordered {

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationStartingEvent) {
            beforeStarting();
        }
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }

    private void beforeStarting() {
        System.setProperty("user.cache", FileUtils.validateDirectoryExists(JvmUtils.getCacheDirectory()).getAbsolutePath());
    }
}
