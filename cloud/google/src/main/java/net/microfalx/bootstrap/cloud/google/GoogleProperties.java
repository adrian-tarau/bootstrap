package net.microfalx.bootstrap.cloud.google;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("bootstrap.cloud.google")
@Getter
@Setter
@ToString
public class GoogleProperties {

    /**
     * API key for Google Maps services. If not provided, Google Maps services will be disabled.
     */
    private String mapApiKey;

    /**
     * Map identifier for Google Maps services.
     */
    private String mapId;
}
