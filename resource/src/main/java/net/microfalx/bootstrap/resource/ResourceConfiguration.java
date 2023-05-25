package net.microfalx.bootstrap.resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Holds configuration for resource service.
 */
@Configuration
@ConfigurationProperties("bootstrap.resource")
public class ResourceConfiguration {

    @Value("${user.home}/.bootstrap/data")
    private String persistedDirectory;

    @Value("${user.home}/.bootstrap/storage")
    private String transientDirectory;

    @Value("${user.home}/.bootstrap/shared")
    private String sharedDirectory;
    private String sharedUserName;
    private String sharedPassword;

    public String getPersistedDirectory() {
        return persistedDirectory;
    }

    public String getTransientDirectory() {
        return transientDirectory;
    }

    public String getSharedDirectory() {
        return sharedDirectory;
    }

    public String getSharedUserName() {
        return sharedUserName;
    }

    public String getSharedPassword() {
        return sharedPassword;
    }
}
