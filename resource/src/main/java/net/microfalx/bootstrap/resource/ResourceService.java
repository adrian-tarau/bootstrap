package net.microfalx.bootstrap.resource;

import jakarta.annotation.PostConstruct;
import net.microfalx.resource.FileResource;
import net.microfalx.resource.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

import static net.microfalx.resource.ResourceUtils.requireNonNull;

/**
 * A service which provides standard locations for resources.
 */
@Service
public class ResourceService {

    private File persistedDirectory;
    private File transientDirectory;

    @Autowired
    private ResourceConfiguration configuration;

    /**
     * Returns the resource directory for persisted data.
     * <p>
     * Loss of persisted data does result in loss of critical data.
     *
     * @param name the subdirectory name
     * @return a non-null instance
     */
    public Resource getPersisted(String name) {
        return get(ResourceLocation.PERSISTED, name);
    }

    /**
     * Returns the resource directory for transient data.
     * <p>
     * Loss of transient data does not result in loss of critical data.
     *
     * @param name the subdirectory name
     * @return a non-null instance
     */
    public Resource getTransient(String name) {
        return get(ResourceLocation.TRANSIENT, name);
    }

    /**
     * Returns the resource directory for shared data.
     * <p>
     * Shared data is stored outside the process and it is usually a remote file system.
     *
     * @param name the subdirectory name
     * @return a non-null instance
     */
    public Resource getShared(String name) {
        return get(ResourceLocation.SHARED, name);
    }

    /**
     * Returns the resource directory.
     *
     * @param location the type of storage
     * @param name     the subdirectory name
     * @return a non-null instance
     */
    public Resource get(ResourceLocation location, String name) {
        requireNonNull(location);
        requireNonNull(name);

        File directory = switch (location) {
            case PERSISTED -> new File(configuration.getPersistedDirectory(), name);
            case TRANSIENT -> new File(configuration.getTransientDirectory(), name);
            case SHARED -> new File(configuration.getSharedDirectory(), name);
        };
        return FileResource.file(directory).resolve(name, Resource.Type.DIRECTORY);
    }

    @PostConstruct
    protected void initialize() {
        validateDirectory(persistedDirectory = new File(configuration.getPersistedDirectory()));
        validateDirectory(transientDirectory = new File(configuration.getTransientDirectory()));
    }

    private void validateDirectory(File directory) {
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IllegalStateException("Directory " + directory + " cannot be created");
        }
    }
}
