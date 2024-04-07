package net.microfalx.bootstrap.template;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties("bootstrap.template")
public class TemplateProperties {

    private boolean cached = true;

    private Duration cacheExpiration = Duration.ofSeconds(50);

    public boolean isCached() {
        return cached;
    }

    public void setCached(boolean cached) {
        this.cached = cached;
    }

    public Duration getCacheExpiration() {
        return cacheExpiration;
    }

    public void setCacheExpiration(Duration cacheExpiration) {
        this.cacheExpiration = cacheExpiration;
    }
}
