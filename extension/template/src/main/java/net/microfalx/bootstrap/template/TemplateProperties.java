package net.microfalx.bootstrap.template;

import lombok.Getter;
import lombok.Setter;
import net.microfalx.bootstrap.core.config.BootstrapProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

import static java.time.Duration.ofSeconds;

@Configuration
@ConfigurationProperties("bootstrap.template")
@Getter
@Setter
public class TemplateProperties {

    @Autowired(required = false) private BootstrapProperties bootstrap = new BootstrapProperties();

    /**
     * Flag, true (default) to cache the templates
     */
    private boolean cached = true;

    /**
     * Duration to cache the templates, default 60 seconds
     */
    private Duration cacheExpiration = ofSeconds(60);

    /**
     * Returns whether the templates are cached.
     *
     * @return a non-null instance
     */
    public boolean isCached() {
        if (bootstrap.isDebug()) {
            return false;
        } else {
            return cached;
        }
    }
}
