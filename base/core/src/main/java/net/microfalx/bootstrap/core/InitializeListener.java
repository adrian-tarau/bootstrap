package net.microfalx.bootstrap.core;

import net.microfalx.bootstrap.core.utils.BootUtils;
import net.microfalx.lang.FileUtils;
import net.microfalx.lang.JvmUtils;
import net.microfalx.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

import static net.microfalx.lang.StringUtils.formatMessage;

public class InitializeListener implements ApplicationListener<ApplicationEvent>, Ordered {

    private volatile ConfigurableEnvironment environment;

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationStartingEvent) {
            logJvmInfo();
            beforeStarting();
        } else if (event instanceof ApplicationStartedEvent startedEvent) {
            initShutdownHook(startedEvent.getApplicationContext());
            logOptions();
        } else if (event instanceof ApplicationEnvironmentPreparedEvent environmentPreparedEvent) {
            environment = environmentPreparedEvent.getEnvironment();
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
        if (!JvmUtils.isClient() || BootUtils.isCli()) return;
        String jvmSettings = formatMessage("home: ''{0}'', version: ''{1}'', vm info: ''{2}''",
                System.getProperty("java.home"), Runtime.version(), System.getProperty("java.vm.info"));
        getLogger().warn("C2 is disabled, the JVM is running in client mode and this will result in poor performance. " +
                "To enable C2, use the JVM option -XX:+TieredCompilation or make sure -XX:-TieredCompilation is not present." +
                " JVM settings: {}", jvmSettings);
    }

    private void logOptions() {
        if (environment == null) return;
        if (environment.getProperty("bootstrap.debug", Boolean.class, false) && !BootUtils.isCli()) {
            getLogger().warn("Bootstrap debug mode is enabled. Do not enable debug mode in production since it will slow down the application");
        }
    }

    private void initShutdownHook(ConfigurableApplicationContext applicationContext) {
        applicationContext.registerShutdownHook();
    }

    private Logger getLogger() {
        return LoggerFactory.getLogger(InitializeListener.class);
    }


}
