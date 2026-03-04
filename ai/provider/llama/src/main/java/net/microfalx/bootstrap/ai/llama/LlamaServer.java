package net.microfalx.bootstrap.ai.llama;

import lombok.extern.slf4j.Slf4j;
import net.microfalx.bootstrap.ai.api.AiException;
import net.microfalx.bootstrap.ai.api.Chat;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.JvmUtils;
import net.microfalx.lang.TimeUtils;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
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
    private File standardOutput;
    private File standardError;
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
     * Stops the server.
     */
    public void stop() {
        LOGGER.info("Stopping server {}", getId());
        process.destroy();
    }

    public void ping() {
        lastPing = System.currentTimeMillis();
    }

    void start() {
        standardOutput = factory.getLog(chat, ".out");
        standardError = factory.getLog(chat, ".err");
        File workingDirectory = factory.getExecutable().getParentFile();
        ProcessBuilder builder = new ProcessBuilder(buildCommandLine()).directory(workingDirectory);
        builder.redirectOutput(standardOutput).redirectError(standardError);
        try {
            process = builder.start();
        } catch (Exception e) {
            throw new AiException("Failed to start llama server", e);
        }
        factory.getThreadPool().execute(new Worker());
    }

    private List<String> buildCommandLine() {
        List<String> command = new ArrayList<>();
        command.add(factory.getExecutable().getAbsolutePath());
        port = JvmUtils.getNextAvailablePort(51456);
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
