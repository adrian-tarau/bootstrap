package net.microfalx.bootstrap.security.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.jdbc.entity.NamedTimestampAware;
import net.microfalx.lang.annotation.Description;
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
    private String userName;

    @Column(name = "enabled", nullable = false)
    @Position(10)
    @Description("Indicates whether the {name} is enabled or disabled")
    private boolean enabled;

    @Column(name = "password", nullable = false)
    @NotBlank
    @Visible(false)
    private String password;

    @Column(name = "email")
    @Position(20)
    @Description("The email associated with a {name}")
    private String email;
}
