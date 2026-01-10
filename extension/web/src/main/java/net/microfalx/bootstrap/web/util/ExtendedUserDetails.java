package net.microfalx.bootstrap.web.util;

import net.microfalx.lang.Descriptable;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * An extended "principal" which provided more information to the web interface.
 */
public interface ExtendedUserDetails extends UserDetails, AuthenticatedPrincipal, Descriptable {

    /**
     * Returns the full name of the user (usually first name + last name).
     *
     * @return a non-null instance
     */
    default String getDisplayName() {
        return getName();
    }

    /**
     * Returns the email address associated with the user.
     *
     * @return the email, null/empty if not available
     */
    String getEmail();

    /**
     * Returns the URL of the image associated with the user.
     *
     * @return the URL, null if not available
     */
    String getImageUrl();

    /**
     * Returns whether if the user is an external user (authenticated via an external identity provider).
     *
     * @return {@code true} if the user is external, {@code false} otherwise
     */
    boolean isExternal();

    /**
     * Returns whether the user is required to reset the password on next login.
     *
     * @return {@code true} if the user must reset the password, {@code false} otherwise
     */
    boolean isResetPassword();
}
