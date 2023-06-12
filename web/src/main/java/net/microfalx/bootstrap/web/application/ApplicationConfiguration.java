package net.microfalx.bootstrap.web.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {

    @Autowired
    private ApplicationService applicationService;

    @Bean(name = "application")
    public Application getApplication() {
        return applicationService.getApplication();
    }
}
