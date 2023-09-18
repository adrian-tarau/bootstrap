package net.microfalx.bootstrap.search;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Holds configuration for search engine.
 */
@Configuration
@ConfigurationProperties("bootstrap.search")
public class SearchProperties {

    private boolean allowLeadingWildcard = true;

    public boolean isAllowLeadingWildcard() {
        return allowLeadingWildcard;
    }

    public void setAllowLeadingWildcard(boolean allowLeadingWildcard) {
        this.allowLeadingWildcard = allowLeadingWildcard;
    }
}
