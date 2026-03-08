package net.microfalx.bootstrap.ai.llama;

import lombok.extern.slf4j.Slf4j;
import net.microfalx.bootstrap.ai.api.AiNotAvailableException;
import net.microfalx.bootstrap.ai.api.Chat;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.lang.*;
import net.microfalx.resource.Resource;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.isNotEmpty;
import static net.microfalx.lang.TimeUtils.millisSince;

/**
 * Holds one instance
 */
@Slf4j
public class LlamaServer implements Identifiable<String> {

    private final LlamaServerFactory factory;
    private final Chat chat;
    private Process process;
    private int port;
    private volatile int exitCode;
    private File logFile;
    private final CountDownLatch latch = new CountDownLatch(1);
    private volatile Map<String, Object> properties;
    private volatile boolean started;
    private volatile boolean available;
    private volatile long lastPing;

    LlamaServer(LlamaServerFactory factory, Chat chat) {
        requireNonNull(factory);
        requireNonNull(chat);
        this.factory = factory;
        this.chat = chat;
    }

    @Override
    public String getId() {
        return chat.getId();
    }

    /**
     * Returns the port where the server is listening.
     *
     * @return a positive integer
     */
    public int getPort() {
        return port;
    }

    /**
     * Returns the URL to access the server.
     *
     * @return a non-null instance
     */
    public URI getUri() {
        return getUri(false);
    }

    /**
     * Returns the URL to access the server.
     *
     * @param base {@code true} to create only base URI, {@code false} to create complete URI
     * @return a non-null instance
     */
    public URI getUri(boolean base) {
        return URI.create("http://localhost:" + getPort() + (base ? "" : "/v1"));
    }

    /**
     * Returns whether the server is idle (no chat communication).
     *
     * @return {@code true} if the server is idle, {@code false} otherwise
     */
    public boolean isIdle() {
        return millisSince(lastPing) > TimeUtils.FIVE_MINUTE;
    }

    /**
     * Returns whether the server is started and running.
     *
     * @return {@code true} if the server is started, {@code false} otherwise
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * Returns whether the server is available to accept new requests.
     *
     * @return {@code true} if the server is available, {@code false} otherwise
     */
    public boolean isAvailable() {
        ConcurrencyUtils.await(latch);
        return available;
    }

    /**
     * Stops the server.
     */
    public void stop() {
        LOGGER.info("Stopping server {}", getId());
        try {
            process.destroy();
        } finally {
            started = false;
        }
    }

    /**
     * Returns the logs of the server, including both standard output and standard error.
     *
     * @return a non-null instance
     */
    public Resource getLogs() {
        StringBuilder builder = new StringBuilder();
        append(builder, logFile);
        return Resource.text(builder.toString());

    }

    /**
     * Returns the properties of the server, including configuration and runtime information.
     *
     * @return a non-null instance
     */
    public Map<String, Object> getProperties() {
        if (properties != null) return properties;
        URI uri = URI.create(getUri(true) + "/props");
        HttpRequest request = HttpRequest.newBuilder().uri(uri)
                .header("Content-Type", "application/json")
                .GET().build();
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                properties = Field.from(response.body(), Map.class);
            } else {
                properties = Collections.emptyMap();
                LOGGER.error("Failed to get properties from server {}, status code {}, body {}", getId(), response.statusCode(), response.body());
            }

        } catch (Exception e) {
            LOGGER.atError().setCause(e).log("Failed to get properties from server {}", getId());
        }
        return properties;
    }

    public void ping() {
        lastPing = System.currentTimeMillis();
    }

    void start(boolean async) {
        logFile = factory.getLog(chat, ".log");
        port = JvmUtils.getNextAvailablePort(51456);
        File workingDirectory = factory.getExecutable().getParentFile();
        ProcessBuilder builder = new ProcessBuilder(buildCommandLine()).directory(workingDirectory);
        builder.redirectOutput(logFile).redirectErrorStream(true);
        if (async) {
            factory.getThreadPool().execute(() -> doStart(builder));
        } else {
            doStart(builder);
        }
    }

    private void append(StringBuilder builder, File file) {
        String text = null;
        if (file.exists()) text = read(file);
        text = StringUtils.trim(text);
        if (isNotEmpty(text)) builder.append(text).append("\n");
    }

    private String read(File file) {
        try {
            return IOUtils.getInputStreamAsString(IOUtils.getBufferedInputStream(file));
        } catch (Exception e) {
            return "#ERROR for " + file.getAbsolutePath() + ": " + ExceptionUtils.getRootCauseDescription(e);
        }
    }

    private void doStart(ProcessBuilder builder) {
        try {
            process = builder.start();
            available = true;
        } catch (Exception e) {
            throw new AiNotAvailableException("Failed to start llama server", e);
        }
        factory.getThreadPool().execute(new Worker());
        started = true;
    }

    private List<String> buildCommandLine() {
        List<String> command = new ArrayList<>();
        command.add(factory.getExecutable().getAbsolutePath());
        command.add("--port");
        command.add(Integer.toString(port));
        command.add("-m");
        command.add(factory.resolve(chat.getModel()).getAbsolutePath());
        return command;
    }

    private class Worker implements Runnable {

        @Override
        public void run() {
            try {
                exitCode = process.waitFor();
            } catch (InterruptedException e) {
                // ignore
            }
            if (exitCode != 0) {
                LOGGER.error("Chat session ended with exit code {}", exitCode);
            }
        }
    }
}
