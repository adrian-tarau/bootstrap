package net.microfalx.bootstrap.security.provisioning;

import jakarta.servlet.http.HttpServletRequest;
import net.microfalx.bootstrap.security.SecurityUtils;
import net.microfalx.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    @Bean
    public OAuth2AuthorizationRequestResolver resolver(ClientRegistrationRepository repo) {
        DefaultOAuth2AuthorizationRequestResolver resolver = new DefaultOAuth2AuthorizationRequestResolver(
                repo, "/oauth2/authorization");
        return new AuthorizationRequestResolver(resolver);
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

    private class AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

        private final OAuth2AuthorizationRequestResolver delegate;

        public AuthorizationRequestResolver(OAuth2AuthorizationRequestResolver delegate) {
            this.delegate = delegate;
        }

        @Override
        public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
            return update(delegate.resolve(request));

        }

        @Override
        public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
            return update(delegate.resolve(request, clientRegistrationId));
        }

        private OAuth2AuthorizationRequest update(OAuth2AuthorizationRequest request) {
            if (request == null) return request;
            String registrationId = request.getAttributes().get(OAuth2ParameterNames.REGISTRATION_ID).toString();
            if ("google".equals(registrationId)) {
                return OAuth2AuthorizationRequest.from(request)
                        .additionalParameters(this::updateAdditionalParameters)
                        .build();
            } else {
                return request;
            }
        }

        private void updateAdditionalParameters(Map<String, Object> parameters) {
            if (StringUtils.isNotEmpty(properties.getGoogleClientDomain())) {
                parameters.put("hd", properties.getGoogleClientDomain());
            }
        }
    }
}
