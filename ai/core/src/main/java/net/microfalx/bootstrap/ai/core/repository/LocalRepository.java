package net.microfalx.bootstrap.ai.core.repository;

import lombok.extern.slf4j.Slf4j;
import net.microfalx.bootstrap.ai.api.AiException;
import net.microfalx.bootstrap.ai.api.AiNotFoundException;
import net.microfalx.bootstrap.ai.api.Model;
import net.microfalx.lang.JvmUtils;
import net.microfalx.resource.Resource;

import java.io.File;
import java.io.IOException;
import java.util.function.Function;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.FileUtils.getFileName;
import static net.microfalx.lang.FileUtils.validateDirectoryExists;
import static net.microfalx.lang.FormatterUtils.formatBytes;

/**
 * A proxy used to convert
 */
@Slf4j
public class LocalRepository {

    private final Function<Model, Resource> proxy;
    private File cacheDirectory;

    public LocalRepository(Function<Model, Resource> proxy) {
        requireNonNull(proxy);
        this.proxy = proxy;
    }

    /**
     * Changes the cache directory.
     *
     * @param cacheDirectory the new cache directory
     * @return self
     */
    public LocalRepository setCacheDirectory(File cacheDirectory) {
        requireNonNull(cacheDirectory);
        this.cacheDirectory = cacheDirectory;
        return this;
    }

    /**
     * Resolves the model to a file (or a directory if the model is made out multiple files), which can
     * be used to load the model.
     *
     * @param model the model
     * @return a non-null instance
     */
    public Resource resolve(Model model) {
        requireNonNull(model);
        String cacheFileName = model.getId();
        if (model.getDownloadUri() != null) cacheFileName = getFileName(model.getDownloadUri().getPath());
        File modelCacheFile = new File(getCacheDirectory(), cacheFileName);
        if (modelCacheFile.exists()) {
            LOGGER.debug("Return model file from cache: {}", modelCacheFile);
            return Resource.file(modelCacheFile);
        }
        Resource resource = proxy.apply(model);
        if (resource == null) {
            throw new AiNotFoundException("Model '" + model.getId() + "' cannot be resolved in any model repository");
        }
        LOGGER.info("Download model file from '{}' for model '{}'", resource.toURI(), model.getName());
        try {
            Resource.file(modelCacheFile).copyFrom(resource);
        } catch (IOException e) {
            throw new AiException("Failed to cache model '" + model.getId() + "' to file " + modelCacheFile, e);
        }
        LOGGER.info("Download model file to '{}', file size {}", modelCacheFile, formatBytes(modelCacheFile.length()));
        return Resource.file(modelCacheFile);
    }

    private File getCacheDirectory() {
        if (cacheDirectory == null) {
            cacheDirectory = validateDirectoryExists(JvmUtils.getCacheDirectory("ai"));
        }
        return cacheDirectory;
    }
}
