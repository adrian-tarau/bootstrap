package net.microfalx.bootstrap.resource;

import net.microfalx.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import static net.microfalx.lang.JvmUtils.replacePlaceholders;

/**
 * Holds configuration for resource service.
 */
@Configuration
@ConfigurationProperties("bootstrap.resource")
public class ResourceProperties {

    @Value(ResourceLocation.PERSISTED_PATH)
    private String persistedDirectory;

    @Value(ResourceLocation.TRANSIENT_PATH)
    private String transientDirectory;

    @Value(ResourceLocation.SHARED_PATH)
    private String sharedDirectory;
    private String sharedUserName;
    private String sharedPassword;

    /**
     * Returns default properties, for standalone use (mostly tests).
     *
     * @return a non-null instance
     */
    public static ResourceProperties get() {
        return new ResourceProperties();
    }

    /**
     * Returns the directory for the persisted data.
     *
     * @return a non-null instance
     */
    public String getPersistedDirectory() {
        return StringUtils.defaultIfEmpty(persistedDirectory, replacePlaceholders(ResourceLocation.PERSISTED_PATH));
    }

    /**
     * Changes the directory for the persisted data.
     *
     * @param persistedDirectory the path to the directory
     */
    public void setPersistedDirectory(String persistedDirectory) {
        this.persistedDirectory = persistedDirectory;
    }

    /**
     * Returns the directory for the transient data.
     *
     * @return a non-null instance
     */
    public String getTransientDirectory() {
        return StringUtils.defaultIfEmpty(transientDirectory, replacePlaceholders(ResourceLocation.TRANSIENT_PATH));
    }

    /**
     * Changes the directory for the transient data.
     *
     * @param transientDirectory the path to the directory
     */
    public void setTransientDirectory(String transientDirectory) {
        this.transientDirectory = transientDirectory;
    }


    /**
     * Returns the URI for the shared data.
     *
     * @return a non-null instance
     */
    public String getSharedDirectory() {
        return StringUtils.defaultIfEmpty(sharedDirectory, replacePlaceholders(ResourceLocation.SHARED_PATH));
    }

    /**
     * Changes the URI for the shared data.
     *
     * @param sharedDirectory the URI to the directory
     */
    public void setSharedDirectory(String sharedDirectory) {
        this.sharedDirectory = sharedDirectory;
    }

    /**
     * Returns the username for the URI used for shared data.
     *
     * @return the username, null if not set
     */
    public String getSharedUserName() {
        return sharedUserName;
    }

    /**
     * Changes the username for the URI used for shared data.
     *
     * @param sharedUserName the username
     */
    public void setSharedUserName(String sharedUserName) {
        this.sharedUserName = sharedUserName;
    }

    /**
     * Returns the password for the URI used for shared data.
     *
     * @return the username, null if not set
     */
    public String getSharedPassword() {
        return sharedPassword;
    }

    /**
     * Changes the password for the URI used for shared data.
     *
     * @param sharedPassword the password
     */
    public void setSharedPassword(String sharedPassword) {
        this.sharedPassword = sharedPassword;
    }
}
