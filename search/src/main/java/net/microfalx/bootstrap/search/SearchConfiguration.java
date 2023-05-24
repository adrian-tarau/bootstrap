package net.microfalx.bootstrap.search;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Holds configuration for search engine.
 */
@Configuration
@ConfigurationProperties("bootstrap.search")
public class SearchConfiguration {

    private boolean allowLeadingWildcard;

    public boolean isAllowLeadingWildcard() {
        return allowLeadingWildcard;
    }
}
