package net.microfalx.bootstrap.security.user;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

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
    private final String email;
    private final String description;
    private final String imageUrl;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return null;
    }

}
