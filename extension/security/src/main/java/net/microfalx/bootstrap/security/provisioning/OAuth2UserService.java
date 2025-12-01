package net.microfalx.bootstrap.security.provisioning;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;

import static net.microfalx.bootstrap.security.provisioning.OAuth2Utils.getNameAttributeKey;

class OAuth2UserService extends DefaultOAuth2UserService {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        // Keep existing authorities
        var authorities = oAuth2User.getAuthorities();
        // Build an extended OAuth2User
        return new ExtendedOAuth2User(authorities, oAuth2User.getAttributes(), getNameAttributeKey(oAuth2User));
    }
}
