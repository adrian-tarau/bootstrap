package net.microfalx.bootstrap.web.util;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.Collections.emptyList;
import static net.microfalx.lang.StringUtils.defaultIfEmpty;

/**
 * Various shortcuts related to security, such as retrieving the current user, checking permissions, etc.
 */
public class SecurityUtils {

    /**
     * The username for a user which is not authenticated.
     */
    public static final String ANONYMOUS_USER_NAME = "anonymous";

    /**
     * User details about a user called {@link #ANONYMOUS_USER_NAME} with no permissions.
     */
    public static final UserDetails ANONYMOUS_USER = new org.springframework.security.core.userdetails.User(ANONYMOUS_USER_NAME, getRandomPassword(20), emptyList());

    /**
     * A principal based on {@link #ANONYMOUS_USER_NAME}.
     */
    public static final Principal ANONYMOUS_PRINCIPAL = new AnonymousPrincipal();

    /**
     * Returns whether the username identifies the {@link #ANONYMOUS_USER}
     *
     * @param userName the username
     * @return {@code true} if the anonymous user, {@code false} otherwise
     */
    public static boolean isAnonymous(String userName) {
        return ANONYMOUS_USER_NAME.equalsIgnoreCase(userName);
    }

    /**
     * Returns the username of the current principal.
     * <p>
     * If the security context is not available, the method returns {@link #ANONYMOUS_USER_NAME}
     *
     * @return a non-null instance
     */
    public static String getCurrentUserName() {
        return getUserName(getAuthentication());
    }

    /**
     * Returns the username of the given authentication.
     * <p>
     * If the authentication is not available, the method returns {@link #ANONYMOUS_USER_NAME}
     *
     * @param authentication the authentication
     * @return a non-null instance
     */
    public static String getUserName(Authentication authentication) {
        String userName = null;
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                userName = ((UserDetails) principal).getUsername();
            } else if (principal instanceof Principal) {
                userName = ((Principal) principal).getName();
            } else if (principal instanceof String) {
                userName = principal.toString();
            } else if (principal != null){
                userName = principal.toString();
            }
        }
        return defaultIfEmpty(userName, ANONYMOUS_USER_NAME);
    }

    /**
     * Returns the current principal.
     * <p>
     * If the security context is not available, the method returns a principal based on
     * {@link #ANONYMOUS_USER_NAME}.
     *
     * @return a non-null instance
     */
    public static Principal getCurrentPrincipal() {
        Authentication authentication = getAuthentication();
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof Principal) return (Principal) principal;
        }
        return ANONYMOUS_PRINCIPAL;
    }

    /**
     * Returns the user information of the currently authenticated principal.
     * <p>
     * If the security context is not available, the method returns a user based on
     * {@link #ANONYMOUS_USER_NAME}.
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
     * Returns a random generated password.
     *
     * @return a non-null instance
     */
    public static String getRandomPassword(int size) {
        StringBuilder builder = new StringBuilder();
        while (builder.length() < size) {
            builder.append(randomLong());
        }
        return builder.toString();
    }

    /**
     * Returns whether the current authentication is authenticated and not anonymous.
     *
     * @return {@code true} if authenticated, {@code false} otherwise
     */
    public static boolean isAuthenticated() {
        return isAuthenticated(getAuthentication());
    }

    /**
     * Returns whether the given authentication is authenticated and not anonymous.
     *
     * @param authentication the authentication
     * @return {@code true} if authenticated, {@code false} otherwise
     */
    public static boolean isAuthenticated(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)
                && !isAnonymous(authentication.getName());
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

    private static class AnonymousPrincipal implements Principal {

        @Override
        public String getName() {
            return ANONYMOUS_USER_NAME;
        }
    }
}
