package net.microfalx.bootstrap.cloud.google;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collection;

public class GoogleIdTokenAuthenticationToken extends AbstractAuthenticationToken {

    private final String idToken;
    private OidcUser principal;

    public GoogleIdTokenAuthenticationToken(String idToken) {
        super(null);
        this.idToken = idToken;
        setAuthenticated(false);
    }

    public GoogleIdTokenAuthenticationToken(OidcUser principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.idToken = null;
        this.principal = principal;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return idToken;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

}
