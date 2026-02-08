package net.microfalx.bootstrap.cloud.google;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import static net.microfalx.lang.StringUtils.isEmpty;
import static net.microfalx.lang.StringUtils.isNotEmpty;

@Configuration
@ConfigurationProperties("bootstrap.cloud.google")
@Getter
@Setter
@ToString
public class GoogleProperties {

    /**
     * Client ID for Google Identity Services. If not provided, Google Identity Services will be disabled.
     */
    private String clientId;

    /**
     * Client secret for Google Identity Services. If not provided, Google Identity Services will be disabled.
     */
    private String clientSecret;

    /**
     * Whether Google Identity Services is enabled. If not provided, it will be enabled if both client ID are provided.
     */
    private boolean gisEnabled;

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
     * Returns the client ID for Google Identity Services.
     *
     * @return a non-null instance
     */
    public String getClientId() {
        if (isEmpty(clientId)) {
            throw new IllegalStateException("Google Client ID is not set");
        }
        return clientId;
    }

    /**
     * Returns the client secret for Google Identity Services.
     *
     * @return a non-null instance
     */
    public String getClientSecret() {
        if (isEmpty(clientSecret)) {
            throw new IllegalStateException("Google Client Secret is not set");
        }
        return clientSecret;
    }

    /**
     * Returns whether the client id and secret are available.
     *
     * @return {@code true} if enabled, {@code false} otherwise`
     */
    public boolean isClientSecretAvailable() {
        return isNotEmpty(clientId) && isNotEmpty(clientSecret);
    }

    /**
     * Returns whether GIS (client side) is enabled.
     *
     * @return {@code true} if enabled, {@code false} otherwise
     */
    public boolean isGisEnabled() {
        return gisEnabled && isNotEmpty(clientId);
    }

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
     * Returns whether the Google Maps API is enabled.
     *
     * @return {@code true} if enabled, {@code false} otherwise
     */
    public boolean isMapApiEnabled() {
        return !isEmpty(mapApiKey);
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
