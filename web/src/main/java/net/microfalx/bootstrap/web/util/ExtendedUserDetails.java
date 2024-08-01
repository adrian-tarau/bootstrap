package net.microfalx.bootstrap.web.util;

import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * An extended "principal" which provided more information to the web interface.
 */
public interface ExtendedUserDetails extends UserDetails, AuthenticatedPrincipal {

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
}
