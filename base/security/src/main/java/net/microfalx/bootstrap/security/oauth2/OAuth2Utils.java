package net.microfalx.bootstrap.security.oauth2;

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
public class OAuth2Utils {

    private static final String[] USER_NAME_ATTRIBUTES = {"login", StandardClaimNames.EMAIL, IdTokenClaimNames.SUB};
    private static final String[] DISPLAY_NAME_ATTRIBUTES = {StandardClaimNames.NAME, StandardClaimNames.EMAIL};
    private static final String[] EMAIL_ATTRIBUTES = {StandardClaimNames.EMAIL};
    private static final String[] PICTURE_ATTRIBUTES = {StandardClaimNames.PICTURE, "avatar_url"};

    private static final ThreadLocal<Boolean> USER_NAME_LOOKUP = ThreadLocal.withInitial(() -> Boolean.FALSE);

    /**
     * Returns the display name associated with an OAuth2 principal.
     *
     * @param principal the principal to extract the display name from
     * @return the display name, never null
     */
    public static String getDisplayName(OAuth2AuthenticatedPrincipal principal) {
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

    /**
     * Returns the username associated with an OAuth2 principal.
     *
     * @param principal the principal to extract the username from
     * @return the username, never null
     */
    public static String getUserName(OAuth2AuthenticatedPrincipal principal) {
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

    /**
     * Returns the email associated with an OAuth2 principal.
     *
     * @param principal the principal to extract the email from
     * @return the email, null if not available
     */
    public static String getEMail(OAuth2AuthenticatedPrincipal principal) {
        return getAttribute(principal, EMAIL_ATTRIBUTES);
    }

    /**
     * Returns the picture url with an OAuth2 principal.
     *
     * @param principal the principal to extract the picture url from
     * @return the url, null if not available
     */
    public static String getPictureUrl(OAuth2AuthenticatedPrincipal principal) {
        return getAttribute(principal, PICTURE_ATTRIBUTES);
    }

    public static String getNameAttributeKey(OAuth2User user) {
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
