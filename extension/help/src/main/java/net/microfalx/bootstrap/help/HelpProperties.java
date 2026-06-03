package net.microfalx.bootstrap.help;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("bootstrap.help")
@Getter
@Setter
@ToString
public class HelpProperties {

    /**
     * Indicates whether the help should be accessed only if a security context exits.
     */
    private boolean secure = true;
}
