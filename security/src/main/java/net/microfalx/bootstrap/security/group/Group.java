package net.microfalx.bootstrap.security.group;

import net.microfalx.bootstrap.security.user.Role;
import net.microfalx.lang.Descriptable;
import net.microfalx.lang.Nameable;

import java.util.Set;

/**
 * Represents a group of users.
 * <p>
 * A group is identified by a unique ID and has a name and description.
 * It can be used to manage permissions and roles for a set of users.
 */
public interface Group extends Nameable, Descriptable {

    /**
     * Returns the roles associated with this group.
     *
     * @return a non-null instance
     */
    Set<Role> getRoles();
}
