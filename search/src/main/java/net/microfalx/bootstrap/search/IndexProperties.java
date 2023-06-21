package net.microfalx.bootstrap.search;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("bootstrap.index")
public class IndexProperties {

    private long ramBufferSize = -1;
    private long ramBufferSizeThread = -1;

    public long getRamBufferSize() {
        return ramBufferSize;
    }

    public void setRamBufferSize(long ramBufferSize) {
        this.ramBufferSize = ramBufferSize;
    }

    public long getRamBufferSizeThread() {
        return ramBufferSizeThread;
    }

    public void setRamBufferSizeThread(long ramBufferSizeThread) {
        this.ramBufferSizeThread = ramBufferSizeThread;
    }
}
