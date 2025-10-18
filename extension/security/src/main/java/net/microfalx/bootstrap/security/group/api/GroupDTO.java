package net.microfalx.bootstrap.security.group.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.security.group.Group;
import net.microfalx.bootstrap.security.user.Role;
import net.microfalx.bootstrap.security.user.jpa.User;

import java.util.Collection;
import java.util.Set;

@Getter
@Setter
@ToString
@Schema(name = "Group", description = "A security group")
public class GroupDTO implements Group {

    private String name;
    private boolean enabled;
    private Set<Role> roles;
    private Collection<User> users;
    private String description;
}
