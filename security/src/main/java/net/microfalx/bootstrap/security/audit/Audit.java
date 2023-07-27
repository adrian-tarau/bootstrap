package net.microfalx.bootstrap.security.audit;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import net.microfalx.bootstrap.security.user.User;
import net.microfalx.lang.annotation.Position;
import net.microfalx.lang.annotation.ReadOnly;
import net.microfalx.lang.annotation.Visible;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "security_audit")
@ReadOnly
public class Audit {

    public static String OPEN = "Open";
    public static String ADD = "Add";
    public static String EDIT = "Edit";
    public static String DELETE = "Delete";
    public static String EXECUTE = "Execute";

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Visible(false)
    private Integer id;

    @JoinColumn(name = "username", nullable = false)
    @NotNull
    @ManyToOne
    private User user;

    @Column(name = "action", nullable = false)
    @NotBlank
    private String action;

    @Column(name = "created_at", nullable = false, updatable = false)
    @NotNull
    @Position(500)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "description")
    @Position(1000)
    private String description;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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
        if (o == null || getClass() != o.getClass()) return false;
        Audit audit = (Audit) o;
        return Objects.equals(id, audit.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
