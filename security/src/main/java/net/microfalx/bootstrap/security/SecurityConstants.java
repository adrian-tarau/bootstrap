package net.microfalx.bootstrap.security;

/**
 * A collection of constants used by the security module.
 */
public class SecurityConstants {

    /**
     * The user name for a user which is not authenticated
     */
    public static final String ANONYMOUS_USER = "anonymous";

    /**
     * A prefix used with roles by Spring Security.
     */
    public static final String ROLE_PREFIX = "ROLE_";

    /**
     * The admin role
     */
    public static final String ADMIN_ROLE = ROLE_PREFIX + "ADMIN";
}
