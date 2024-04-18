package net.microfalx.bootstrap.store;

import net.microfalx.lang.FormatterUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("bootstrap.store")
public class StoreProperties {

    private long maximumMemorySize = 10 * FormatterUtils.M;

    public long getMaximumMemorySize() {
        return maximumMemorySize;
    }

    public void setMaximumMemorySize(long maximumMemorySize) {
        this.maximumMemorySize = maximumMemorySize;
    }
}
