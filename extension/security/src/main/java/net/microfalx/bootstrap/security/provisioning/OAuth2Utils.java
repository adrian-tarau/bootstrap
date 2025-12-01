package net.microfalx.bootstrap.security.provisioning;

import net.microfalx.lang.StringUtils;
import org.joor.Reflect;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.user.OAuth2User;

import static net.microfalx.lang.StringUtils.isEmpty;
import static net.microfalx.lang.StringUtils.isNotEmpty;

/**
 * Various utilities for OAuth2.
 */
class OAuth2Utils {

    private static final String[] USER_NAME_ATTRIBUTES = {"login", StandardClaimNames.EMAIL, IdTokenClaimNames.SUB};
    private static final String[] DISPLAY_NAME_ATTRIBUTES = {StandardClaimNames.NAME, StandardClaimNames.EMAIL};
    private static final String[] EMAIL_ATTRIBUTES = {StandardClaimNames.EMAIL};
    private static final String[] PICTURE_ATTRIBUTES = {StandardClaimNames.PICTURE, "avatar_url"};

    private static final ThreadLocal<Boolean> USER_NAME_LOOKUP = ThreadLocal.withInitial(() -> Boolean.FALSE);

    static String getDisplayName(OAuth2AuthenticatedPrincipal principal) {
        String displayName = null;
        String firstName = principal.getAttribute(StandardClaimNames.GIVEN_NAME);
        String lastName = principal.getAttribute(StandardClaimNames.FAMILY_NAME);
        if (isNotEmpty(firstName) && isNotEmpty(lastName)) {
            displayName = firstName + " " + lastName;
        } else {
            String name = getAttribute(principal, DISPLAY_NAME_ATTRIBUTES);
            if (isNotEmpty(name)) displayName = name;
        }
        if (isEmpty(displayName)) displayName = principal.getName();
        return displayName;
    }

    static String getUserName(OAuth2AuthenticatedPrincipal principal) {
        String userName = getAttribute(principal, USER_NAME_ATTRIBUTES);
        if (isNotEmpty(userName)) {
            return userName;
        } else {
            if (USER_NAME_LOOKUP.get()) {
                USER_NAME_LOOKUP.set(true);
                try {
                    return principal.getName();
                } finally {
                    USER_NAME_LOOKUP.set(false);
                }
            } else {
                return null;
            }
        }
    }

    static String getEMail(OAuth2AuthenticatedPrincipal principal) {
        return getAttribute(principal, EMAIL_ATTRIBUTES);
    }

    static String getPictureUrl(OAuth2AuthenticatedPrincipal principal) {
        return getAttribute(principal, PICTURE_ATTRIBUTES);
    }

    static String getNameAttributeKey(OAuth2User user) {
        String key = Reflect.on(user).get("nameAttributeKey");
        return StringUtils.defaultIfEmpty(key, IdTokenClaimNames.SUB);
    }

    private static String getAttribute(OAuth2AuthenticatedPrincipal principal, String[] attributeNames) {
        for (String attributeName : attributeNames) {
            String value = principal.getAttribute(attributeName);
            if (isNotEmpty(value)) return value;
        }
        return null;
    }
}
