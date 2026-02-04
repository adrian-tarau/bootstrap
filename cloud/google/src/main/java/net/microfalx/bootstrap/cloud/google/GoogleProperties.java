package net.microfalx.bootstrap.cloud.google;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import static net.microfalx.lang.StringUtils.isEmpty;

@Configuration
@ConfigurationProperties("bootstrap.cloud.google")
@Getter
@Setter
@ToString
public class GoogleProperties {

    /**
     * API key for Google Address services. If not provided, Google Address services will be disabled.
     */
    private String addressApiKey;

    /**
     * API key for Google Maps services. If not provided, Google Maps services will be disabled.
     */
    private String mapApiKey;

    /**
     * Map identifier for Google Maps services.
     */
    private String mapId;

    /**
     * Returns the Google Address API key.
     *
     * @return a non-null instance
     */
    public String getAddressApiKey() {
        if (isEmpty(addressApiKey)) {
            throw new IllegalStateException("Google Address API key is not set");
        }
        return addressApiKey;
    }

    /**
     * Returns the Google Maps API key.
     *
     * @return a non-null instance
     */
    public String getMapApiKey() {
        if (isEmpty(mapApiKey)) {
            throw new IllegalStateException("Google Address API key is not set");
        }
        return mapApiKey;
    }

    /**
     * Returns the Google Maps Map ID.
     *
     * @return a non-null instance
     */
    public String getMapId() {
        if (isEmpty(mapId)) {
            throw new IllegalStateException("Google Address API key is not set");
        }
        return mapId;
    }
}
