package net.microfalx.bootstrap.search;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

import static java.time.Duration.ofSeconds;

/**
 * Holds configuration for search engine.
 */
@Configuration
@ConfigurationProperties("bootstrap.search")
public class SearchProperties {

    private boolean allowLeadingWildcard = true;
    private Duration refreshInterval = ofSeconds(60);

    public boolean isAllowLeadingWildcard() {
        return allowLeadingWildcard;
    }

    public void setAllowLeadingWildcard(boolean allowLeadingWildcard) {
        this.allowLeadingWildcard = allowLeadingWildcard;
    }

    public Duration getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(Duration refreshInterval) {
        this.refreshInterval = refreshInterval;
    }
}
