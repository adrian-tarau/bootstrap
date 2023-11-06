package net.microfalx.bootstrap.security.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Component;
import net.microfalx.bootstrap.jdbc.entity.NamedTimestampAware;
import net.microfalx.lang.annotation.Description;
import net.microfalx.lang.annotation.Label;
import net.microfalx.lang.annotation.Position;
import net.microfalx.lang.annotation.Visible;

@Entity
@Table(name = "security_users")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true,callSuper = false)
@ToString
public class User extends NamedTimestampAware {

    @Id
    @Column(name = "username", nullable = false)
    @NotBlank
    @Position(1)
    @EqualsAndHashCode.Include
    @Description("The user name associated with a {name}")
    @Visible(modes = Visible.Mode.ADD)
    private String userName;

    @Column(name = "password", nullable = false)
    @NotBlank
    @Visible(modes = Visible.Mode.ADD)
    @Component(Component.Type.PASSWORD)
    private String password;

    @Transient
    @Visible(modes = Visible.Mode.ADD)
    @Label("Retype Password")
    @Component(Component.Type.PASSWORD)
    private String retypePassword;

    @Column(name = "enabled", nullable = false)
    @Position(10)
    @Description("Indicates whether the {name} is enabled or disabled")
    private boolean enabled;

    @Column(name = "email")
    @Position(20)
    @Description("The email associated with a {name}")
    private String email;
}
