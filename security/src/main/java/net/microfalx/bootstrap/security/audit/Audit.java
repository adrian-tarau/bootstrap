package net.microfalx.bootstrap.security.audit;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Component;
import net.microfalx.bootstrap.dataset.annotation.OrderBy;
import net.microfalx.bootstrap.security.user.User;
import net.microfalx.lang.annotation.Position;
import net.microfalx.lang.annotation.ReadOnly;
import net.microfalx.lang.annotation.Visible;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "security_audit")
@ReadOnly
@ToString(callSuper = true)
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
    @Position(10)
    private User user;

    @Column(name = "action", nullable = false)
    @NotBlank
    @Position(2)
    private String action;

    @Column(name = "category", nullable = false)
    @NotBlank
    @Position(3)
    private String category;

    @Column(name = "client_info", nullable = false)
    @NotBlank
    @Position(11)
    private String clientInfo;

    @Column(name = "created_at", nullable = false, updatable = false)
    @NotNull
    @Position(1)
    @OrderBy(OrderBy.Direction.DESC)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "reference", nullable = false)
    @NotBlank
    @Position(30)
    private String reference;

    @Column(name = "error_code", nullable = false)
    @NotBlank
    @Position(31)
    private String errorCode;

    @Column(name = "description")
    @Position(1000)
    @Component(Component.Type.TEXT_AREA)
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getClientInfo() {
        return clientInfo;
    }

    public void setClientInfo(String clientInfo) {
        this.clientInfo = clientInfo;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
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
