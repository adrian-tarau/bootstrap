package net.microfalx.bootstrap.trace.startup;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.metrics.ApplicationStartup;

/**
 * Registers a custom {@link ApplicationStartup} if the default one is provided.
 */
public class StartupApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        if (applicationContext.getApplicationStartup() == ApplicationStartup.DEFAULT) {
            applicationContext.setApplicationStartup(new ApplicationStartupImpl());
        }
    }
}
