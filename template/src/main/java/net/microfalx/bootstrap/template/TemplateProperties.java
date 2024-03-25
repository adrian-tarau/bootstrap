package net.microfalx.bootstrap.template;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("bootstrap.template")
public class TemplateProperties {

    private boolean cache = true;
}
