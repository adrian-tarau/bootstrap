package net.microfalx.bootstrap.cloud.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

//@Component
@Slf4j
public class GoogleIdTokenAuthenticationProvider implements AuthenticationProvider {

    private final GoogleIdentityService googleIdentityService;
    private final ClientRegistration googleRegistration;

    public GoogleIdTokenAuthenticationProvider(GoogleIdentityService googleIdentityService, ClientRegistrationRepository registrations) {
        this.googleIdentityService = googleIdentityService;
        this.googleRegistration = registrations.findByRegistrationId("google");
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        String tokenValue = (String) authentication.getCredentials();
        GoogleIdToken token = googleIdentityService.verify(tokenValue);
        GoogleIdToken.Payload payload = token.getPayload();
        LOGGER.info("Received Google ID token for user: {}, payload: {}", payload.getEmail(), payload);
        Map<String, Object> claims = payload.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Instant issuedAt = Instant.ofEpochSecond(payload.getIssuedAtTimeSeconds());
        Instant expiresAt = Instant.ofEpochSecond(payload.getExpirationTimeSeconds());
        OidcIdToken oidcIdToken = new OidcIdToken(tokenValue, issuedAt, expiresAt, claims);

        OidcUser user = new DefaultOidcUser(Collections.emptyList(), oidcIdToken, "email");
        return new OAuth2AuthenticationToken(user, user.getAuthorities(), googleRegistration.getRegistrationId());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return GoogleIdTokenAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
