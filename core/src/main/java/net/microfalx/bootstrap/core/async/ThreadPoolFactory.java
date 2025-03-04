package net.microfalx.bootstrap.core.async;

import net.microfalx.threadpool.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A factory which builds a {@link net.microfalx.threadpool.ThreadPool} with the most common settings.
 * <p>
 * The settings can be customized using {@link AsynchronousProperties}.
 */
public class ThreadPoolFactory {

    static final Logger LOGGER = LoggerFactory.getLogger(ThreadPoolFactory.class);

    private AsynchronousProperties properties = new AsynchronousProperties();
    private float ratio = 1;

    public static ThreadPoolFactory create(String suffix) {
        return new ThreadPoolFactory().setSuffix(suffix);
    }

    public static ThreadPoolFactory create(AsynchronousProperties properties) {
        return new ThreadPoolFactory().setProperties(properties);
    }

    public AsynchronousProperties getProperties() {
        return properties;
    }

    public ThreadPoolFactory setProperties(AsynchronousProperties properties) {
        requireNonNull(properties);
        this.properties = properties;
        return this;
    }

    public ThreadPoolFactory setSuffix(String suffix) {
        requireNonNull(suffix);
        properties.setSuffix(suffix);
        return this;
    }

    public ThreadPoolFactory setQueueCapacity(int queueCapacity) {
        properties.setQueueCapacity(queueCapacity);
        return this;
    }

    public ThreadPoolFactory setRatio(float ratio) {
        this.ratio = ratio;
        return this;
    }

    public ThreadPool create() {
        ThreadPool.Builder builder = ThreadPool.builder(properties.getThreadNamePrefix())
                .maximumSize((int) (properties.getCoreThreads() * ratio))
                .queueSize((int) (properties.getQueueCapacity() * ratio));
        builder.virtual(properties.isVirtual())
                .keepAliveTime(properties.getKeepAlive());
        return builder.build();
    }
}
