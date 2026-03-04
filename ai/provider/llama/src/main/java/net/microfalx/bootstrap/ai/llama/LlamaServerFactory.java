package net.microfalx.bootstrap.ai.llama;

import lombok.extern.slf4j.Slf4j;
import net.microfalx.bootstrap.ai.api.AiException;
import net.microfalx.bootstrap.ai.api.Chat;
import net.microfalx.bootstrap.ai.api.Model;
import net.microfalx.bootstrap.ai.core.repository.LocalRepository;
import net.microfalx.lang.JvmUtils;
import net.microfalx.resource.ClassPathResource;
import net.microfalx.resource.Resource;
import net.microfalx.resource.ResourceUtils;
import net.microfalx.threadpool.ThreadPool;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.FileUtils.validateDirectoryExists;

/**
 * A server wrapper around the Llama Server.
 */
@Slf4j
public class LlamaServerFactory {

    private static volatile LlamaServerFactory instance = new LlamaServerFactory();

    private final Map<String, LlamaServer> servers = new ConcurrentHashMap<>();
    private Function<Model, File> repository = new RepositoryImpl();
    private File nativeDirectory;
    private volatile File nativeExecutable;
    private volatile ThreadPool threadPool;
    private File cacheDirectory;
    private File logsDirectory;

    public static LlamaServerFactory getInstance() {
        if (instance == null) {
            synchronized (LlamaServerFactory.class) {
                if (instance == null) {
                    instance = new LlamaServerFactory();
                }
            }
        }
        return instance;
    }

    private LlamaServerFactory() {
        initialize();
    }

    /**
     * Stops all servers.
     */
    public void shutdown() {
        for (LlamaServer server : servers.values()) {
            server.stop();
        }
    }

    /**
     * Changes the model repository.
     *
     * @param repository the new repository
     */
    public void setRepository(Function<Model, File> repository) {
        requireNonNull(repository);
        this.repository = repository;
    }

    /**
     * Resolves a model to a local file.
     * <p>
     * The model is downloaded from the internet and cached locally.
     *
     * @param model the model
     * @return the location of the model
     */
    public File resolve(Model model) {
        requireNonNull(model);
        return repository.apply(model);
    }

    /**
     * Starts a new server to support a chat session.
     *
     * @param chat the chat session
     * @return a non-null instance
     */
    public LlamaServer start(Chat chat) {
        requireNonNull(chat);
        LOGGER.info("Start server for chat '{}'", chat.getId());
        LlamaServer server = new LlamaServer(this, chat);
        server.start();
        LOGGER.info("Server for chat '{}' started at {}", chat.getId(), server.getUri(true));
        servers.put(server.getId(), server);
        return server;
    }

    /**
     * Stops an existing server.
     *
     * @param server the server instance
     */
    void stop(LlamaServer server) {
        requireNonNull(server);
        LOGGER.info("Stop server '{}'", server.getId());
        servers.remove(server.getId());
    }

    File getExecutable() {
        if (nativeExecutable == null) {
            synchronized (this) {
                if (nativeExecutable == null) {
                    nativeExecutable = copyExecutable();
                }
            }
        }
        return nativeExecutable;
    }

    ThreadPool getThreadPool() {
        if (threadPool == null) {
            synchronized (this) {
                if (threadPool == null) {
                    threadPool = ThreadPool.builder("Llama").maximumSize(10).build();
                }
            }
        }
        return threadPool;
    }

    File getLog(Chat chat, String suffix) {
        if (logsDirectory == null) {
            logsDirectory = validateDirectoryExists(JvmUtils.getVariableDirectory("llama"));
        }
        return new File(logsDirectory, chat.getId() + suffix);
    }

    private File getNativeDirectory() {
        if (nativeDirectory == null) {
            nativeDirectory = validateDirectoryExists(new File(JvmUtils.getNativeDirectory(), "llama"));
        }
        return nativeDirectory;
    }

    private File getCacheDirectory() {
        if (cacheDirectory == null) {
            cacheDirectory = validateDirectoryExists(JvmUtils.getCacheDirectory("llama"));
        }
        return cacheDirectory;
    }

    private File copyExecutable() {
        Resource sourceDirectory = ClassPathResource.directory("llama-native/" + getNativeExecutableDirectory());
        Resource targetDirectory = Resource.directory(getNativeDirectory());
        Resource sourceExecutable = sourceDirectory.resolve(getNativeExecutableFileName());
        Resource targetExecutable = Resource.file(new File(getNativeDirectory(), getNativeExecutableFileName()));
        try {
            if (!targetExecutable.exists() || targetExecutable.length() != sourceExecutable.length()) {
                for (Resource sourceResource : sourceDirectory.list()) {
                    targetDirectory.resolve(sourceResource.getFileName()).copyFrom(sourceResource);
                }
            }
        } catch (IOException e) {
            throw new AiException("Failed to setup native executable to: " + targetDirectory, e);
        }
        return ResourceUtils.toFile(targetExecutable);
    }

    private String getNativeExecutableDirectory() {
        if (JvmUtils.isWindows()) {
            return "win";
        } else if (JvmUtils.isLinux()) {
            return "linux";
        } else {
            throw new IllegalStateException("Unsupported platform");
        }
    }

    private String getNativeExecutableFileName() {
        if (JvmUtils.isWindows()) {
            return "llama-server.exe";
        } else if (JvmUtils.isLinux()) {
            return "llama-server";
        } else {
            throw new IllegalStateException("Unsupported platform");
        }
    }

    private void initialize() {
        LOGGER.info("Start Llama Server");
        getThreadPool().scheduleAtFixedRate(new Maintenance(), Duration.ofSeconds(60));
        Runtime.getRuntime().addShutdownHook(new ShutdownThread());
    }

    private void stopIdle() {
        for (LlamaServer server : servers.values()) {
            if (!server.isIdle()) continue;
            LOGGER.info("Stopping idle server {}", server.getId());
            server.stop();
        }
    }

    private static class RepositoryImpl implements Function<Model, File> {

        private final LocalRepository localRepository = new LocalRepository(new LocalRepositoryProxy());

        @Override
        public File apply(Model model) {
            return ResourceUtils.toFile(localRepository.resolve(model));
        }
    }

    private static class LocalRepositoryProxy implements Function<Model, Resource> {

        @Override
        public Resource apply(Model model) {
            try {
                Resource url = Resource.url(model.getDownloadUri().toURL());
                return url.exists() ? url : null;
            } catch (IOException e) {
                // ignore, it means it does not exist
            }
            return null;
        }
    }

    private class Maintenance implements Runnable {

        @Override
        public void run() {
            stopIdle();
        }
    }

    private class ShutdownThread extends Thread {

        @Override
        public void run() {
            LlamaServerFactory.this.shutdown();
        }
    }

}
