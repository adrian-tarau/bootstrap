package net.microfalx.bootstrap.security.provisioning;

import net.microfalx.bootstrap.security.SecurityUtils;
import net.microfalx.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;

import java.util.ArrayList;
import java.util.List;

import static net.microfalx.lang.StringUtils.isNotEmpty;

@Configuration
public class OAuth2ClientConfiguration {

    @Autowired(required = false)
    private OAuth2Properties properties = new OAuth2Properties();

    @Autowired(required = false)
    private SecurityProperties settings = new SecurityProperties();

    @Bean
    public ClientRegistrationRepository repository() {
        List<ClientRegistration> registrations = new ArrayList<>();
        if (settings.isEnabled() && isNotEmpty(properties.getGoogleClientId())) {
            registrations.add(google());
        }
        if (settings.isEnabled() && isNotEmpty(properties.getGithubClientId())) {
            registrations.add(github());
        }
        if (settings.isEnabled() && isNotEmpty(properties.getAzureClientId())) {
            registrations.add(azure());
        }
        if (registrations.isEmpty()) registrations.add(noop());
        return new InMemoryClientRegistrationRepository(registrations);
    }

    private ClientRegistration google() {
        return CommonOAuth2Provider.GOOGLE
                .getBuilder("google")
                .clientId(properties.getGoogleClientId())
                .clientSecret(properties.getGoogleClientSecret())
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .tokenUri("https://oauth2.googleapis.com/token")
                .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
                .userInfoUri("https://openidconnect.googleapis.com/v1/userinfo")
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .build();
    }

    private ClientRegistration github() {
        return CommonOAuth2Provider.GITHUB
                .getBuilder("github")
                .clientId(properties.getGithubClientId())
                .clientSecret(properties.getGithubClientSecret())
                .scope("read:user", "user:email")
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .clientName("GitHub")
                .build();
    }

    private ClientRegistration azure() {
        String tenantId = StringUtils.defaultIfEmpty(properties.getAzureTenantId(), "common");
        String base = "https://login.microsoftonline.com/" + tenantId + "/oauth2/v2.0";
        return ClientRegistration.withRegistrationId("azure")
                .clientId(properties.getAzureClientId())
                .clientSecret(properties.getAzureClientSecret())
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("openid", "profile", "email", "offline_access")
                .authorizationUri(base + "/authorize")
                .tokenUri(base + "/oauth2/v2.0/token")
                .jwkSetUri("https://login.microsoftonline.com/common/discovery/v2.0/keys")
                .userInfoUri("https://graph.microsoft.com/oidc/userinfo")
                .userNameAttributeName(IdTokenClaimNames.SUB)
                .clientName("Microsoft")
                .build();
    }

    private ClientRegistration noop() {
        return CommonOAuth2Provider.GOOGLE
                .getBuilder("noop")
                .clientId(SecurityUtils.getRandomPassword())
                .clientSecret(SecurityUtils.getRandomPassword())
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .build();
    }
}
