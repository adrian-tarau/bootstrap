package net.microfalx.bootstrap.core.async;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

import static java.time.Duration.ofSeconds;
import static net.microfalx.lang.ArgumentUtils.*;

@Configuration
@ConfigurationProperties("bootstrap.async")
@Setter
@Getter
public class AsynchronousProperties {

    private int coreThreads = 5;
    private int maximumThreads = 8;
    private int queueCapacity = 50;
    private String prefix = "boot";
    private String suffix;
    private boolean waitForTasks = true;
    private boolean removeOnCancel = true;
    private Duration keepAlive = ofSeconds(5);
    private Duration awaitTermination = ofSeconds(30);

    public AsynchronousProperties setCoreThreads(int coreThreads) {
        requireBounded(maximumThreads, 1, 500);
        this.coreThreads = coreThreads;
        if (maximumThreads < coreThreads) maximumThreads = (int) (coreThreads + coreThreads * 0.2f);
        return this;
    }

    public AsynchronousProperties setMaximumThreads(int maximumThreads) {
        requireBounded(maximumThreads, 1, 1000);
        this.maximumThreads = maximumThreads;
        return this;
    }

    public AsynchronousProperties setQueueCapacity(int queueCapacity) {
        requireBounded(queueCapacity, 10, 5000);
        this.queueCapacity = queueCapacity;
        return this;
    }

    public AsynchronousProperties setPrefix(String prefix) {
        requireNotEmpty(prefix);
        this.prefix = prefix;
        return this;
    }

    public AsynchronousProperties setKeepAlive(Duration keepAlive) {
        requireNonNull(keepAlive);
        this.keepAlive = keepAlive;
        return this;
    }

    public AsynchronousProperties setAwaitTermination(Duration awaitTermination) {
        requireNonNull(awaitTermination);
        this.awaitTermination = awaitTermination;
        return this;
    }
}
