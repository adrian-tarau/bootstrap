package net.microfalx.bootstrap.security.provisioning;

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import static net.microfalx.bootstrap.security.provisioning.OAuth2Utils.getNameAttributeKey;

class OidcUserService extends org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService {

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);
        // Keep existing authorities
        var authorities = oidcUser.getAuthorities();
        // Build an extended OidcUser
        return new ExtendedOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo(), getNameAttributeKey(oidcUser));
    }

}
