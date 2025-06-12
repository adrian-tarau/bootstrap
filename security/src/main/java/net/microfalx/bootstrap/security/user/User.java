package net.microfalx.bootstrap.security.user;

import net.microfalx.lang.Descriptable;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;

public interface User extends Identifiable<String>, Nameable, Descriptable {

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
