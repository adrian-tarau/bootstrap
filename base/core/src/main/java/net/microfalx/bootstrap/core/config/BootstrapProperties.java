package net.microfalx.bootstrap.core.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("bootstrap")
@Getter
@Setter
@ToString
public class BootstrapProperties {

    /**
     * A global flag which enables all the debug options across all services.
     */
    private boolean debug;
}
