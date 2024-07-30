package net.microfalx.bootstrap.security.group;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.jdbc.entity.NamedAndTimestampedIdentityAware;
import net.microfalx.bootstrap.security.user.User;
import net.microfalx.lang.annotation.Description;
import net.microfalx.lang.annotation.Position;
import net.microfalx.lang.annotation.Visible;

import java.util.Collection;

@Entity
@Table(name = "security_groups")
@Getter
@Setter
@ToString
public class Group extends NamedAndTimestampedIdentityAware<Integer> {

    @Column(name = "enabled", nullable = false)
    @Position(10)
    @Description("Indicates whether the {name} is enabled or disabled")
    private boolean enabled;

    @Visible(value = false)
    @ManyToMany()
    @JoinTable(name = "security_group_members", joinColumns = @JoinColumn(name = "group_id"), inverseJoinColumns = @JoinColumn(name = "username"))
    private Collection<User> users;
}
