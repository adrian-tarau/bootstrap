package net.microfalx.bootstrap.security;

import net.microfalx.bootstrap.security.userinfo.ExtendedUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;

/**
 * Various utilities around security
 */
public class SecurityUtils {

    /**
     * User details about a user called {@link SecurityConstants#ANONYMOUS_USER_NAME} with no permissions.
     */
    public static final UserDetails ANONYMOUS_USER = net.microfalx.bootstrap.web.util.SecurityUtils.ANONYMOUS_USER;

    /**
     * A principal based on {@link SecurityConstants#ANONYMOUS_USER_NAME}.
     */
    public static final Principal ANONYMOUS_PRINCIPAL = net.microfalx.bootstrap.web.util.SecurityUtils.ANONYMOUS_PRINCIPAL;

    /**
     * Returns a random generated password.
     *
     * @return a non-null instance
     */
    public static String getRandomPassword() {
        return getRandomPassword(20);
    }

    /**
     * Returns a random generated password.
     *
     * @return a non-null instance
     */
    public static String getRandomPassword(int size) {
        return net.microfalx.bootstrap.web.util.SecurityUtils.getRandomPassword(size);
    }

    /**
     * Returns a normalized version of the username (trimmed and lower-cased).
     *
     * @param userName the user name
     * @return a non-null instance
     */
    public static String normalizeUserName(String userName) {
        return userName != null ? userName.trim().toLowerCase() : null;
    }


    /**
     * Returns the display name of the current principal.
     * <p>
     * If the security context is not available, the method returns {@link SecurityConstants#ANONYMOUS_USER_NAME}.
     *
     * @return a non-null instance
     */
    public static String getDisplayName(Principal principal) {
        if (principal instanceof ExtendedUserDetails user) {
            return user.getDisplayName();
        } else {
            return principal.getName();
        }
    }

    /**
     * Returns the username of the given authentication.
     * <p>
     * If the authentication is not available, the method returns {@link #ANONYMOUS_USER}
     *
     * @param authentication the authentication
     * @return a non-null instance
     */
    public static String getUserName(Authentication authentication) {
        return net.microfalx.bootstrap.web.util.SecurityUtils.getUserName(authentication);
    }

    /**
     * Returns the username of the current principal.
     * <p>
     * If the security context is not available, the method returns {@link #ANONYMOUS_USER}
     *
     * @return a non-null instance
     */
    public static String getCurrentUserName() {
        return net.microfalx.bootstrap.web.util.SecurityUtils.getCurrentUserName();
    }

}
