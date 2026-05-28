package net.microfalx.bootstrap.resource;

import lombok.extern.slf4j.Slf4j;
import net.microfalx.resource.ClassPathResource;
import net.microfalx.resource.Resource;
import net.microfalx.resource.ResourceUtils;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Collections.unmodifiableCollection;
import static net.microfalx.lang.StringUtils.removeStartSlash;

/**
 * Manages class path resources.
 * <p>
 * If the application is in development mode (under IDE), it will detect the original sources
 * of the resources and use them before.
 */
@Slf4j
class ClassPathManager {

    private final Collection<Resource> resources = new CopyOnWriteArraySet<>();
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final Map<String, Boolean> exists = new ConcurrentHashMap<>();

    Resource resolve(URI uri) {
        if (shouldInitialize()) initialize();
        String path = uri.getPath();
        if (!resources.isEmpty()) {
            Resource resource = resolve(path);
            if (resource != null) return resource;
        }
        return ClassPathResource.create(path);
    }

    void initialize() {
        if (!shouldInitialize()) return;
        for (File directory : getDirectories()) {
            discoverResourceDirectory(directory);
        }
    }

    Collection<Resource> getResources() {
        return unmodifiableCollection(resources);
    }

    Collection<File> getDirectories() {
        String classpath = System.getProperty("java.class.path");
        String[] entries = classpath.split(File.pathSeparator);
        Collection<File> directories = new ArrayList<>();
        for (String entry : entries) {
            if (entry.endsWith(".jar")) continue;
            File file = new File(entry);
            if (file.isDirectory()) directories.add(file);
        }
        return directories;
    }

    private boolean shouldInitialize() {
        return initialized.compareAndSet(false, true);
    }

    private void discoverResourceDirectory(File directory) {
        int maxDepth = 3;
        while (maxDepth-- > 0 && directory != null) {
            if (isModuleRoot(directory)) {
                registerModule(directory);
                break;
            }
            directory = directory.getParentFile();
        }
    }

    private void registerModule(File directory) {
        File resources = new File(new File(new File(directory, "src"), "main"), "resources");
        if (resources.exists()) {
            this.resources.add(Resource.directory(resources));
        }
    }

    private boolean isModuleRoot(File directory) {
        for (String moduleFile : MODULE_FILES) {
            if (new File(directory, moduleFile).exists()) return true;
        }
        return false;
    }

    private Resource resolve(String path) {
        if (path.endsWith("/")) return null;
        path = removeStartSlash(path);
        Boolean pathExists = exists.get(path);
        if (pathExists != null && !pathExists) return null;
        Resource resolved = null;
        for (Resource resource : resources) {
            resolved = resource.resolve(path);
            if (ResourceUtils.exists(resolved)) {
                break;
            } else {
                resolved = null;
            }
        }
        exists.put(path, resolved != null);
        return resolved;
    }

    private static final String[] MODULE_FILES = {
            "pom.xml", "build.gradle"
    };


}
