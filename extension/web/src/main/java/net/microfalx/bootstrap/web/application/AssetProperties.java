package net.microfalx.bootstrap.web.application;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.core.config.BootstrapProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("bootstrap.asset")
@Getter
@Setter
@ToString
public class AssetProperties {

    @Autowired(required = false) private BootstrapProperties bootstrap;

    /**
     * Flag, indicates whether the assets are in debug mode. When in debug mode, they are not cached
     * and additional information is logged/traced.
     */
    private boolean debug;

    public boolean isDebug() {
        return debug || bootstrap.isDebug();
    }
}
