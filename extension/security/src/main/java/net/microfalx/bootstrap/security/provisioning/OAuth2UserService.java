package net.microfalx.bootstrap.security.provisioning;

import net.microfalx.bootstrap.security.user.UserService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.HashSet;

import static net.microfalx.bootstrap.security.provisioning.OAuth2Utils.getNameAttributeKey;

class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserService userService;
    private final OAuth2Properties properties;

    public OAuth2UserService(UserService userService, OAuth2Properties properties) {
        this.userService = userService;
        this.properties = properties;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        // Keep existing authorities
        Collection<GrantedAuthority> authorities = new HashSet<>(oAuth2User.getAuthorities());
        // Build an extended OAuth2User
        ExtendedOAuth2User user = new ExtendedOAuth2User(authorities, oAuth2User.getAttributes(), getNameAttributeKey(oAuth2User));
        // extract the local user details
        UserDetails userDetails = userService.findUserDetails(user.getUsername());
        // merge the authorities given by administrators, in addition to those provided by the OIDC provider
        if (properties.isUseLocalAuthorization() && userDetails != null) {
            authorities.addAll(userDetails.getAuthorities());
        }
        return new ExtendedOAuth2User(authorities, oAuth2User.getAttributes(), getNameAttributeKey(oAuth2User));
    }
}
