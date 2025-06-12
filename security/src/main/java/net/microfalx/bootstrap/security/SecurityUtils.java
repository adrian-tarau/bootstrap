package net.microfalx.bootstrap.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.Collections.emptyList;

/**
 * Various utilities around security
 */
public class SecurityUtils {

    /**
     * User details about a user called {@link SecurityConstants#ANONYMOUS_USER} with no permissions.
     */
    public static final UserDetails ANONYMOUS_USER = new org.springframework.security.core.userdetails.User(SecurityConstants.ANONYMOUS_USER, getRandomPassword(), emptyList());

    /**
     * Returns a random generated password.
     *
     * @return a non-null instance
     */
    public static String getRandomPassword() {
        return randomLong() + randomLong();
    }

    /**
     * Returns whether the username identifies the {@link SecurityConstants#ANONYMOUS_USER}
     *
     * @param userName the username
     * @return {@code true} if the anonymous user, {@code false} otherwise
     */
    public static boolean isAnonymous(String userName) {
        return SecurityConstants.ANONYMOUS_USER.equalsIgnoreCase(userName);
    }

    /**
     * Returns the username of the current principal.
     * <p>
     * If the security context is not available, the method returns {@link SecurityConstants#ANONYMOUS_USER}
     *
     * @return a non-null instance
     */
    public static String getCurrentUserName() {
        return getUserName(getAuthentication());
    }

    /**
     * Returns the username of the given authentication.
     * <p>
     * If the authentication is not available, the method returns {@link SecurityConstants#ANONYMOUS_USER}
     *
     * @param authentication the authentication
     * @return a non-null instance
     */
    public static String getUserName(Authentication authentication) {
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) return ((UserDetails) principal).getUsername();
        }
        return SecurityConstants.ANONYMOUS_USER;
    }

    /**
     * Returns the current principal.
     * <p>
     * If the security context is not available, the method returns a principal based on
     * {@link SecurityConstants#ANONYMOUS_USER}.
     *
     * @return a non-null instance
     */
    public static Principal getCurrentPrincipal() {
        Authentication authentication = getAuthentication();
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof Principal) return (Principal) principal;
        }
        return () -> SecurityConstants.ANONYMOUS_USER;
    }

    /**
     * Returns the user information of the currently authenticated principal.
     * <p>
     * If the security context is not available, the method returns a user based on
     * {@link SecurityConstants#ANONYMOUS_USER}.
     *
     * @param authentication the authentication
     * @return a non-null instance
     */
    public static UserDetails getUserDetails(Authentication authentication) {
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                return (UserDetails) principal;
            }
        }
        return ANONYMOUS_USER;
    }

    /**
     * Returns the currently authenticated principal or an authentication request token.
     *
     * @return the authenticated principal, null if one is not available
     */
    static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private static String randomLong() {
        return Long.toString(Math.abs(ThreadLocalRandom.current().nextLong()), Character.MAX_RADIX);
    }
}
