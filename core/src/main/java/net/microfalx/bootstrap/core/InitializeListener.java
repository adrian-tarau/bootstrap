package net.microfalx.bootstrap.core;

import net.microfalx.lang.FileUtils;
import net.microfalx.lang.JvmUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;

import static net.microfalx.lang.StringUtils.formatMessage;

public class InitializeListener implements ApplicationListener<ApplicationEvent>, Ordered {

    private static final Logger LOGGER = LoggerFactory.getLogger(InitializeListener.class);

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationStartingEvent) {
            logJvmInfo();
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

    private void logJvmInfo() {
        if (!JvmUtils.isClient()) return;
        String jvmSettings = formatMessage("home: ''{0}'', version: ''{1}'', vm info: ''{2}''",
                System.getProperty("java.home"), Runtime.version(), System.getProperty("java.vm.info"));
        LOGGER.error("C2 is disabled, the JVM is running in client mode and this will result in poor performance. " +
                "To enable C2, use the JVM option -XX:+TieredCompilation or make sure -XX:-TieredCompilation is not present." +
                " JVM settings: {}", jvmSettings);
    }
}
