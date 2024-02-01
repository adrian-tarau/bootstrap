package net.microfalx.bootstrap.help;

import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("bootstrap.help")
@ToString
public class HelpProperties {
}
