package net.microfalx.bootstrap.security.user;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;

/**
 * Simple implementation of the {@link User} interface.
 */
@Builder
@Getter
@ToString
class UserImpl implements User {

    private final String id;
    private final String name;
    private final String displayName;
    private final String userName;
    private final boolean enabled;
    private final boolean external;
    private final boolean resetPassword;
    private final String email;
    private final String description;
    private final String imageUrl;
    private Collection<GrantedAuthority> authorities = Collections.emptyList();

    @Override
    public String getPassword() {
        return null;
    }
}
