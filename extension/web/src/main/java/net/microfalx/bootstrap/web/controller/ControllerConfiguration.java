package net.microfalx.bootstrap.web.controller;

import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ControllerConfiguration {

    private final ServerProperties serverProperties;

    public ControllerConfiguration(ServerProperties serverProperties) {
        this.serverProperties = serverProperties;
    }

    @Bean
    public DefaultErrorAttributes errorAttributes() {
        return new DefaultErrorAttributes();
    }

    @Bean
    public ErrorProperties errorProperties() {
        ErrorProperties errorProperties = serverProperties.getError();
        errorProperties.getWhitelabel().setEnabled(false);
        return errorProperties;
    }
}
