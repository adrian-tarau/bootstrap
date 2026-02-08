package net.microfalx.bootstrap.security.provisioning;

import net.microfalx.bootstrap.security.user.UserService;
import net.microfalx.bootstrap.security.userinfo.ExtendedOidcUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collection;
import java.util.HashSet;

import static net.microfalx.bootstrap.security.oauth2.OAuth2Utils.getNameAttributeKey;

class OidcUserService extends org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService {

    private final UserService userService;
    private final OAuth2Properties properties;

    public OidcUserService(UserService userService, OAuth2Properties properties) {
        this.userService = userService;
        this.properties = properties;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);
        // Keep existing authorities
        Collection<GrantedAuthority> authorities = new HashSet<>(oidcUser.getAuthorities());
        // Build an extended OidcUser
        ExtendedOidcUser user = new ExtendedOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo(), getNameAttributeKey(oidcUser));
        // extract the local user details
        UserDetails userDetails = userService.findUserDetails(user.getUsername());
        // merge the authorities given by administrators, in addition to those provided by the OIDC provider
        if (properties.isUseLocalAuthorization() && userDetails != null) {
            authorities.addAll(userDetails.getAuthorities());
        }
        return new ExtendedOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo(), getNameAttributeKey(oidcUser));
    }

}
