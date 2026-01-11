package net.microfalx.bootstrap.security.provisioning;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import static net.microfalx.lang.StringUtils.isNotEmpty;

@Configuration
@ConfigurationProperties("bootstrap.security.oauth2")
@Setter
@Getter
@ToString
public class OAuth2Properties {

    private String googleClientId;
    private String googleClientSecret;

    private String githubClientId;
    private String githubClientSecret;

    private String azureTenantId;
    private String azureClientId;
    private String azureClientSecret;

    private boolean useLocalAuthorization = true;

    public boolean isEnabled() {
        return isGoogleEnabled() || isGithubEnabled() || isAzureEnabled();
    }

    public boolean isGoogleEnabled() {
        return isNotEmpty(googleClientId);
    }

    public boolean isGithubEnabled() {
        return isNotEmpty(githubClientId);
    }

    public boolean isAzureEnabled() {
        return isNotEmpty(azureClientId);
    }
}
