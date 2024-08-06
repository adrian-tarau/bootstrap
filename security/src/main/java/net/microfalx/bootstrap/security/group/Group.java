package net.microfalx.bootstrap.security.group;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.jdbc.entity.NamedAndTimestampedIdentityAware;
import net.microfalx.bootstrap.security.user.Role;
import net.microfalx.bootstrap.security.user.User;
import net.microfalx.lang.annotation.Description;
import net.microfalx.lang.annotation.Position;
import net.microfalx.lang.annotation.Visible;

import java.util.Collection;
import java.util.Set;

@Entity
@Table(name = "security_groups")
@Getter
@Setter
@ToString(exclude = "users", callSuper = true)
public class Group extends NamedAndTimestampedIdentityAware<Integer> {

    @Column(name = "enabled", nullable = false)
    @Position(10)
    @Description("Indicates whether the {name} is enabled or disabled")
    private boolean enabled;

    @Transient
    @Position(20)
    private Set<Role> roles;

    @Visible(value = false)
    @ManyToMany()
    @JoinTable(name = "security_group_members", joinColumns = @JoinColumn(name = "group_id"), inverseJoinColumns = @JoinColumn(name = "username"))
    private Collection<User> users;
}
