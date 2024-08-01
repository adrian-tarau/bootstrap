package net.microfalx.bootstrap.security.user;

import lombok.ToString;
import net.microfalx.lang.IdentityAware;
import net.microfalx.lang.NamedIdentityAware;

@ToString(callSuper = true)
public class Role extends NamedIdentityAware<String> {

    public static Role ADMIN = (Role) Role.create("admin").name("Admin").build();
    public static Role GUEST = (Role) Role.create("guest").name("Guest").build();

    public static Builder create(String id) {
        return new Builder(id);
    }

    public static class Builder extends NamedIdentityAware.Builder<String> {

        public Builder(String id) {
            super(id);
        }

        @Override
        protected IdentityAware<String> create() {
            return new Role();
        }

        public Role build() {
            Role role = (Role) super.build();
            return role;
        }
    }
}
