package net.microfalx.bootstrap.security.user;

import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;

import java.security.Principal;

/**
 * An interface for representing a security context.
 * <p>
 * A security context is a container for security-related information
 * such as user identity, roles, and permissions.
 */
public interface SecurityContext extends Identifiable<String>, Nameable, org.springframework.security.core.context.SecurityContext {

    /**
     * Returns the security context associated with the current thread/session.
     *
     * @return a non-null instance of User
     */
    static SecurityContext get() {
        return SecurityContextImpl.CONTEXT.get();
    }

    /**
     * Returns the application user associated with this security context.
     *
     * @return a non-null instance of User
     */
    User getUser();

    /**
     * Returns whether the user has a specific role.
     *
     * @param role the role to check
     * @return {@code true} if the user has the role, {@code false} otherwise
     */
    boolean hasRole(String role);

    /**
     * Returns the principal associated with this security context.
     *
     * @return a non-null instance of Principal
     */
    Principal getPrincipal();

    /**
     * Returns the roles associated with this security context.
     *
     * @return an array of roles, never null
     */
    <T> T getAttribute(String name);

    /**
     * Sets an attribute in the security context.
     *
     * @param name  the name of the attribute
     * @param value the value of the attribute
     */
    <T> void setAttribute(String name, T value);
}
