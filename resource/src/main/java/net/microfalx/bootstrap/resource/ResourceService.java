package net.microfalx.bootstrap.resource;

import net.microfalx.resource.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.resource.ResourceUtils.toUri;

/**
 * A service which provides standard locations for resources.
 * <p>
 * The service provides three types of locations:
 * <ul>
 *     <li>transient - a location for temporary resources, which are not guaranteed to survive process restart</li>
 *     <li>persistent - a location for persisted resources, local to the server where the process runs, which will survive process restarts</li>
 *     <li>shared - a location for persisted resources outside the local server</li>
 * </ul>
 */
@Service
public class ResourceService implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceService.class);

    private File persistedDirectory;
    private File transientDirectory;

    private Resource sharedResource;

    @Autowired
    private ResourceProperties properties = new ResourceProperties();

    /**
     * Returns a resource directory for persisted data.
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
     * Returns a resource directory for transient data.
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
     * Returns a resource directory for shared data.
     * <p>
     * Shared data is stored outside the process (most of the time), and it is usually a remote file system (object store like S3, etc).
     *
     * @param name the subdirectory name
     * @return a non-null instance
     */
    public Resource getShared(String name) {
        return get(ResourceLocation.SHARED, name);
    }

    /**
     * Returns the resource directory.
     * <p>
     * For shared resource, they can be access also as
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
        initializeSharedResource();
        validateResource(getSharedResource(null));
        LOGGER.info("Persisted resources directory {}", persistedDirectory);
        ResourceFactory.setWorkspace(FileResource.directory(persistedDirectory));
        LOGGER.info("Transient resources directory {}", transientDirectory);
        ResourceFactory.setTemporary(FileResource.directory(new File(transientDirectory, "tmp")));
        LOGGER.info("Shared resources directory {}", getSharedResource(null).toURI());
    }

    private void initializeSharedResource() {
        UserPasswordCredential credential = UserPasswordCredential.create(properties.getSharedUserName(), properties.getSharedPassword());
        sharedResource = ResourceFactory.resolve(toUri(properties.getSharedDirectory()), credential, Resource.Type.DIRECTORY);
        ResourceFactory.setShared(sharedResource);
    }

    private Resource getSharedResource(String name) {
        return name != null ? SharedResource.directory(name) : sharedResource;
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
