package net.microfalx.bootstrap.security.user;

import net.microfalx.lang.Descriptable;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;

import java.security.Principal;

/**
 * Represents a user in the system.
 * <p>
 * A user is an entity that can be authenticated and authorized within the system.
 * It extends several interfaces to provide additional information about the user,
 * such as their name, description, and principal identity.
 */
public interface User extends Identifiable<String>, Nameable, Descriptable, Principal {

    /**
     * Returns the display name of the user.
     * <p>
     * The display name is typically first name and last name, or a username;
     *
     * @return a non-null instance
     */
    default String getDisplayName() {
        return getName();
    }

    /**
     * Returns the username of the user.
     *
     * @return a non-null instance
     * @see #getId()
     */
    default String getUserName() {
        return getId();
    }

    /**
     * Returns whether the user is enabled.
     *
     * @return {@code true} if the user is enabled, {@code false} otherwise
     */
    boolean isEnabled();

    /**
     * Return the email address of the user.
     *
     * @return the email address, null if not set
     */
    String getEmail();
}
