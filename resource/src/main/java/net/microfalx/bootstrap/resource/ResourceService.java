package net.microfalx.bootstrap.resource;

import net.microfalx.resource.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A service which provides standard locations for resources.
 */
@Service
public class ResourceService implements InitializingBean {

    private File persistedDirectory;
    private File transientDirectory;

    @Autowired
    private ResourceProperties properties;

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

        return switch (location) {
            case PERSISTED -> get(persistedDirectory, name);
            case TRANSIENT -> get(transientDirectory, name);
            case SHARED -> getSharedResource(name);
        };
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initialize();
    }

    protected void initialize() {
        validateDirectory(persistedDirectory = new File(properties.getPersistedDirectory()));
        validateDirectory(transientDirectory = new File(properties.getTransientDirectory()));
        validateResource(getSharedResource(null));
    }


    private Resource getSharedResource(String name) {
        Resource resource = ResourceFactory.resolve(properties.getSharedDirectory(), UserPasswordCredential.create(properties.getSharedUserName(), properties.getSharedPassword()));
        return name != null ? resource.resolve(name, Resource.Type.DIRECTORY) : resource;
    }

    private Resource get(File directory, String name) {
        return FileResource.directory(directory).resolve(name, Resource.Type.DIRECTORY);
    }

    private void validateDirectory(File directory) {
        if (!directory.exists() && !directory.mkdirs()) {
            throw new ResourceException("Directory " + directory + " cannot be created");
        }
    }

    private void validateResource(Resource directory) {
        try {
            if (!directory.exists()) {
                directory.create();
            }
        } catch (IOException e) {
            throw new ResourceException("Directory " + directory + " cannot be created", e);
        }
    }
}
