package net.microfalx.bootstrap.security.user.jpa;

import jakarta.persistence.*;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Component;
import net.microfalx.bootstrap.dataset.annotation.Filterable;
import net.microfalx.bootstrap.jdbc.entity.surrogate.TimestampAware;
import net.microfalx.bootstrap.security.group.jpa.Group;
import net.microfalx.lang.annotation.*;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static net.microfalx.bootstrap.security.SecurityUtils.normalizeUserName;

@Entity
@Table(name = "security_users")
@Getter
@Setter
@ToString(exclude = "groups", callSuper = true)
public class User extends TimestampAware implements net.microfalx.bootstrap.security.user.User {

    @Id
    @Column(name = "username", nullable = false)
    @NotBlank
    @Position(1)
    @Description("The user name associated with a {name}")
    @Visible(modes = Visible.Mode.ADD)
    private String userName;

    @Column(name = "name", nullable = false)
    @NotBlank
    @Name
    @Position(5)
    @Description("A name for a {name}")
    @Width("200px")
    private String name;

    @Column(name = "password", nullable = false)
    @NotBlank
    @Visible(modes = Visible.Mode.ADD)
    @Component(Component.Type.PASSWORD)
    private String password;

    @Column(name = "token")
    @Visible(false)
    private String token;

    @Transient
    @Visible(modes = Visible.Mode.ADD)
    @Label("Retype Password")
    @Component(Component.Type.PASSWORD)
    private String retypePassword;

    @Column(name = "enabled", nullable = false)
    @Position(10)
    @Description("Indicates whether the {name} is enabled or disabled")
    private boolean enabled;

    @Column(name = "external", nullable = false)
    @ReadOnly
    @Position(11)
    @Description("Indicates whether the user name is external (managed outside the system by an identity provider)")
    private boolean external;

    @Column(name = "reset_password", nullable = false)
    @ReadOnly
    @Position(11)
    @Description("Indicates whether the user is required to change the password at next login (only applicable for non-external users)")
    @Visible(false)
    private boolean resetPassword;

    @Column(name = "email")
    @Position(20)
    @Description("The email associated with a {name}")
    private String email;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "security_group_members", joinColumns = @JoinColumn(name = "username"), inverseJoinColumns = @JoinColumn(name = "group_id"))
    @Position(30)
    private Collection<Group> groups;

    @Transient
    @Visible()
    private String imageUrl;

    @Column(name = "description")
    @Position(1000)
    @Component(Component.Type.TEXT_AREA)
    @Description("A description for a {name}")
    @Width("300px")
    @Filterable()
    private String description;

    @Override
    public String getId() {
        return normalizeUserName(userName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return Objects.equals(userName, user.userName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }
}
