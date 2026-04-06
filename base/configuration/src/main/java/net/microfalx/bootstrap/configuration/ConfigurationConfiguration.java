package net.microfalx.bootstrap.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigurationConfiguration {

    @Bean
    public net.microfalx.bootstrap.configuration.Configuration configuration(ConfigurationService configurationService) {
        return configurationService.getConfiguration();
    }
}
