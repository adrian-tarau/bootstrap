package net.microfalx.bootstrap.security.user;

import java.security.Principal;
import java.util.Objects;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Simple implementation of the {@link Principal} interface.
 */
class PrincipalImpl implements Principal {

    private final String userName;

    PrincipalImpl(String userName) {
        requireNonNull(userName);
        this.userName = userName;
    }

    @Override
    public String getName() {
        return userName;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PrincipalImpl principal)) return false;
        return Objects.equals(userName, principal.userName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(userName);
    }

    @Override
    public String toString() {
        return userName;
    }
}
