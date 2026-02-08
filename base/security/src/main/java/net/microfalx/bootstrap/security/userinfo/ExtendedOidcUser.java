package net.microfalx.bootstrap.security.userinfo;

import net.microfalx.bootstrap.security.oauth2.OAuth2Utils;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;

import static net.microfalx.lang.StringUtils.isEmpty;

public class ExtendedOidcUser extends DefaultOidcUser implements ExtendedUserDetails {

    private String name;
    private String displayName;

    public ExtendedOidcUser(java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> authorities,
                            org.springframework.security.oauth2.core.oidc.OidcIdToken idToken, org.springframework.security.oauth2.core.oidc.OidcUserInfo userInfo, String nameAttributeKey) {
        super(authorities, idToken, userInfo, nameAttributeKey);
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

    @Override
    public String getUsername() {
        return getName();
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
    public boolean isExternal() {
        return true;
    }

    @Override
    public boolean isResetPassword() {
        return false;
    }
}
