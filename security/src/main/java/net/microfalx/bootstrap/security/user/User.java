package net.microfalx.bootstrap.security.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import net.microfalx.bootstrap.jdbc.entity.NamedTimestampAware;
import net.microfalx.lang.annotation.Position;
import net.microfalx.lang.annotation.Visible;

import java.util.Objects;

@Entity
@Table(name = "security_users")
public class User extends NamedTimestampAware {

    @Id
    @Column(name = "username", nullable = false)
    @NotBlank
    @Position(1)
    private String userName;

    @Column(name = "enabled", nullable = false)
    @Position(10)
    private boolean enabled;

    @Column(name = "password", nullable = false)
    @NotBlank
    @Visible(false)
    private String password;

    @Column(name = "email")
    @Position(20)
    private String email;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;

        return Objects.equals(userName, user.userName);
    }

    @Override
    public int hashCode() {
        return userName != null ? userName.hashCode() : 0;
    }
}
