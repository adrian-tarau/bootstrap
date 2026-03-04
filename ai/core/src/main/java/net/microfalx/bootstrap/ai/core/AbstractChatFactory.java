package net.microfalx.bootstrap.ai.core;

import lombok.Getter;
import lombok.Setter;
import net.microfalx.bootstrap.ai.api.Chat;
import net.microfalx.lang.JvmUtils;

import java.io.File;

import static net.microfalx.lang.FileUtils.validateDirectoryExists;
import static net.microfalx.lang.StringUtils.toIdentifier;

/**
 * Base class for chat factories.
 */
public abstract class AbstractChatFactory implements Chat.Factory {

    /**
     * Properties to be used with the chat factory.
     */
    @Getter
    @Setter
    private AiProperties properties = new AiProperties();

    /**
     * Holds a reference to the AI Service
     */
    @Getter
    @Setter
    private AiServiceImpl aiService;

    /**
     * Returns a directory used to cache models.
     *
     * @param name the name of the provider
     * @return a non-null instance
     */
    protected final File getModelCacheDirectory(String name) {
        return validateDirectoryExists(new File(getModelCacheDirectory(), toIdentifier(name)));
    }

    /**
     * Returns a directory used to cache models.
     *
     * @return a non-null instance
     */
    protected final File getModelCacheDirectory() {
        return validateDirectoryExists(new File(getCacheDirectory(), "models"));
    }

    /**
     * Returns a directory used to store data.
     *
     * @param name the name of the provider
     * @return a non-null instance
     */
    protected final File getWorkingDirectory(String name) {
        return validateDirectoryExists(new File(getWorkingDirectory(), toIdentifier(name)));
    }

    /**
     * Returns a directory used to store data.
     *
     * @return a non-null instance
     */
    protected final File getWorkingDirectory() {
        return validateDirectoryExists(new File(getCacheDirectory(), "workspace"));
    }

    /**
     * Returns a directory used by AI.
     * @return a non-null instance
     */
    protected final File getCacheDirectory() {
        return validateDirectoryExists(JvmUtils.getCacheDirectory("ai"));
    }
}
