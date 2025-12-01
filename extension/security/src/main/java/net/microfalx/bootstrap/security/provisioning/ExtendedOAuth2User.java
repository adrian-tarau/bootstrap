package net.microfalx.bootstrap.security.provisioning;

import net.microfalx.bootstrap.web.util.ExtendedUserDetails;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.Collection;
import java.util.Map;

import static net.microfalx.lang.StringUtils.isEmpty;

class ExtendedOAuth2User extends DefaultOAuth2User implements ExtendedUserDetails {

    private String name;
    private String displayName;

    public ExtendedOAuth2User(Collection<? extends GrantedAuthority> authorities, Map<String, Object> attributes, String nameAttributeKey) {
        super(authorities, attributes, nameAttributeKey);
    }

    public String getDisplayName() {
        if (isEmpty(displayName)) displayName = OAuth2Utils.getDisplayName(this);
        return displayName;
    }

    @Override
    public String getName() {
        if (isEmpty(name)) {
            name = OAuth2Utils.getUserName(this);
            name = (name != null) ? name : super.getName();
        }
        return name;
    }

    @Override
    public String getDescription() {
        return null;
    }

    public String getEmail() {
        return OAuth2Utils.getEMail(this);
    }

    public String getImageUrl() {
        return OAuth2Utils.getPictureUrl(this);
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return getName();
    }
}
