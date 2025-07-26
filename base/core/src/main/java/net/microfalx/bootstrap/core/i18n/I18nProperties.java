package net.microfalx.bootstrap.core.i18n;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Set;

@Configuration
@ConfigurationProperties("bootstrap.i18n")
public class I18nProperties {

    private Set<String> baseNames = new HashSet<>();

    public Set<String> getBaseNames() {
        return baseNames;
    }

    public void setBaseNames(Set<String> baseNames) {
        this.baseNames = baseNames;
    }
}
