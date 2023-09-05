package net.microfalx.bootstrap.core.async;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

import static net.microfalx.lang.ArgumentUtils.requireBounded;

@Configuration
@ConfigurationProperties("bootstrap.async")
public class AsyncProperties {

    private int coreThreads = 5;
    private int maximumThreads = 8;
    private int queueCapacity = 50;
    private String prefix = "boostrap";
    private String suffix;
    private boolean waitForTasks = true;
    private boolean removeOnCancel = true;
    private Duration keepAlive = Duration.ofSeconds(5);
    private Duration awaitTermination = Duration.ofSeconds(30);

    public int getCoreThreads() {
        return coreThreads;
    }

    public void setCoreThreads(int coreThreads) {
        this.coreThreads = coreThreads;
    }

    public int getMaximumThreads() {
        return maximumThreads;
    }

    public void setMaximumThreads(int maximumThreads) {
        this.maximumThreads = maximumThreads;
    }

    public int getQueueCapacity() {
        return queueCapacity;
    }

    public void setQueueCapacity(int queueCapacity) {
        requireBounded(queueCapacity, 10, 5000);
        this.queueCapacity = queueCapacity;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public boolean isWaitForTasks() {
        return waitForTasks;
    }

    public void setWaitForTasks(boolean waitForTasks) {
        this.waitForTasks = waitForTasks;
    }

    public Duration getKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(Duration keepAlive) {
        this.keepAlive = keepAlive;
    }

    public boolean isRemoveOnCancel() {
        return removeOnCancel;
    }

    public void setRemoveOnCancel(boolean removeOnCancel) {
        this.removeOnCancel = removeOnCancel;
    }

    public Duration getAwaitTermination() {
        return awaitTermination;
    }

    public void setAwaitTermination(Duration awaitTermination) {
        this.awaitTermination = awaitTermination;
    }
}
