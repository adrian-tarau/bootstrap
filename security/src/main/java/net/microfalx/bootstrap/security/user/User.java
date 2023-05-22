package net.microfalx.bootstrap.security.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import net.microfalx.bootstrap.jdbc.entity.TimestampAware;

import java.util.Objects;

@Entity
@Table(name = "users")
public class User extends TimestampAware {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "username", nullable = false)
    @NotBlank
    private String userName;

    @Column(name = "enabled", nullable = false)
    private boolean active;

    @Column(name = "password", nullable = false)
    @NotBlank
    private String password;

    @Column(name = "email", nullable = false)
    @NotBlank
    private String email;

    @Column(name = "roles", nullable = false)
    @NotBlank
    private String roles;

    @Column(name = "description")
    private String description;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;

        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
